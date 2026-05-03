import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
import SearchResultCard from '../components/SearchResultCard';
import { trendingSearches } from '../data/categories';
import { useSavedPins } from '../context/SavedPinsContext';
import { Pin } from '../types/pin';

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [typeFilter, setTypeFilter] = useState<'all' | 'pins' | 'boards'>('all');
  const [sortOption, setSortOption] = useState<'relevance' | 'recent'>('relevance');
  const { savedPins } = useSavedPins();
  const [remotePins, setRemotePins] = useState<Pin[]>([]);
  const [remoteBoards, setRemoteBoards] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const query = searchParams.get('q')?.toLowerCase().trim() ?? '';

  const combinedPins = useMemo<Pin[]>(() => {
    const ids = new Set<string>();
    const merged: Pin[] = [];

    savedPins.forEach((pin) => {
      if (!ids.has(pin.id)) {
        merged.push(pin);
        ids.add(pin.id);
      }
    });

    remotePins.forEach((pin) => {
      if (!ids.has(pin.id)) {
        merged.push(pin);
        ids.add(pin.id);
      }
    });

    return merged;
  }, [savedPins, remotePins]);

  useEffect(() => {
    const controller = new AbortController();

    const fetchResults = async () => {
      if (!query) {
        setRemotePins([]);
        setRemoteBoards([]);
        setError(null);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        const response = await axios.get(`${API_BASE_URL}/search`, {
          params: {
            query,
            type: typeFilter === 'all' ? 'ALL' : typeFilter.toUpperCase(),
            sort: sortOption === 'recent' ? 'RECENT' : 'RELEVANCE',
          },
          signal: controller.signal,
        });

        const { pins: backendPins, boards: backendBoards } = response.data || {};

        setRemotePins(
          (backendPins || []).map((pin: any) => ({
            id: String(pin.id),
            title: pin.title,
            description: pin.description ?? '',
            category: pin.boardName ?? 'Uncategorized',
            imageUrl: pin.thumbnailUrl ?? '',
            board: pin.boardName,
            author: {
              name: pin.ownerName ?? pin.ownerUsername ?? 'Creator',
            },
            stats: {
              saves: Number(pin.saveCount ?? 0),
              shares: Number(pin.shareCount ?? 0),
              likes: Number(pin.likeCount ?? 0),
            },
            createdAt: pin.createdAt ?? new Date().toISOString(),
            keywords: pin.keywords ? Array.from(pin.keywords) : [],
          }))
        );

        setRemoteBoards(
          (backendBoards || []).map((board: any) => ({
            id: String(board.id),
            title: board.name,
            description: board.description ?? '',
            category: board.ownerName ?? 'Curator',
            coverUrl: board.coverUrl,
            ownerName: board.ownerName,
            ownerUsername: board.ownerUsername,
            createdAt: board.createdAt ?? new Date().toISOString(),
            updatedAt: board.updatedAt ?? new Date().toISOString(),
            resultType: 'Board' as const,
          }))
        );
      } catch (err: any) {
        if (axios.isCancel(err)) return;
        console.error('Failed to fetch search results:', err);
        setError('Unable to load search results. Please try again.');
        setRemotePins([]);
        setRemoteBoards([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchResults();

    return () => controller.abort();
  }, [query, typeFilter, sortOption]);

  const results = useMemo(() => {
    if (!query) {
      return [];
    }

    const queryWords = query.toLowerCase().split(/\s+/).filter((w) => w.length > 0);

    const pinResults =
      typeFilter === 'all' || typeFilter === 'pins'
        ? combinedPins
            .filter((pin) => {
              const searchableText = [
                pin.title,
                pin.description,
                pin.category,
                pin.author.name,
                pin.author.location || '',
                ...(pin.keywords || []),
                ...(pin.palette || []),
              ]
                .join(' ')
                .toLowerCase();
              
              // Match if all query words are found
              return queryWords.every((word) => searchableText.includes(word));
            })
            .map((pin) => ({ ...pin, resultType: 'Pin' as const }))
        : [];

    const boardResults =
      typeFilter === 'all' || typeFilter === 'boards'
        ? remoteBoards.filter((board) => {
            const searchableText = [board.title, board.description, board.category]
              .join(' ')
              .toLowerCase();
            return queryWords.every((word) => searchableText.includes(word));
          })
        : [];

    const combined = [...pinResults, ...boardResults];

    if (sortOption === 'relevance') {
      // Sort by relevance: exact title matches first, then partial matches
      return combined.sort((a, b) => {
        const aTitleMatch = a.title.toLowerCase().startsWith(query.toLowerCase()) ? 0 : 1;
        const bTitleMatch = b.title.toLowerCase().startsWith(query.toLowerCase()) ? 0 : 1;
        if (aTitleMatch !== bTitleMatch) return aTitleMatch - bTitleMatch;
        return a.title.localeCompare(b.title);
      });
    }

    // Sort by date (newest first)
    return combined.sort((a, b) => {
      const aDate = 'createdAt' in a ? new Date(a.createdAt).getTime() : 0;
      const bDate = 'createdAt' in b ? new Date(b.createdAt).getTime() : 0;
      return bDate - aDate;
    });
  }, [query, sortOption, typeFilter, combinedPins, remoteBoards]);

  return (
    <main className="container py-4 py-md-5 search-page">
      <header className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4 mb-md-5">
        <div>
          <p className="text-muted mb-1 fw-semibold small">🔍 Search results</p>
          <h1 className="h3 mb-0 fw-bold">"{query || 'Start typing to search pins & boards'}"</h1>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <select
            className="form-select shadow-sm"
            value={typeFilter}
            onChange={(event) => setTypeFilter(event.target.value as any)}
          >
            <option value="all">Pins & Boards</option>
            <option value="pins">Pins only</option>
            <option value="boards">Boards only</option>
          </select>
          <select
            className="form-select shadow-sm"
            value={sortOption}
            onChange={(event) => setSortOption(event.target.value as any)}
          >
            <option value="relevance">Sort by relevance</option>
            <option value="recent">Newest</option>
          </select>
        </div>
      </header>

      {isLoading ? (
        <section className="empty-search bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-2">Searching…</p>
        </section>
      ) : error ? (
        <section className="empty-search bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-danger mb-2">{error}</p>
          <p className="text-muted mb-0">Please adjust your query or try again shortly.</p>
        </section>
      ) : query && results.length > 0 ? (
        <div className="row g-4">
          {results.map((result) => (
            <div key={result.id} className="col-12 col-md-6 col-lg-4">
              <SearchResultCard item={result} />
            </div>
          ))}
        </div>
      ) : (
        <section className="empty-search bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-3 fw-medium">No results yet</p>
          <p className="mb-4">Try one of the trending ideas:</p>
          <div className="d-flex flex-wrap justify-content-center gap-2 mt-3">
            {trendingSearches.map((term) => (
              <span key={term} className="badge bg-body-secondary text-dark px-3 py-2 rounded-pill shadow-sm">
                {term}
              </span>
            ))}
          </div>
        </section>
      )}
    </main>
  );
};

export default SearchPage;

