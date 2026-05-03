import React, { useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { boards } from '../data/boards';
import PinGrid from '../components/PinGrid';
import { Pin } from '../types/pin';
import { Collaborator } from '../types/board';
import { API_BASE_URL } from '../config/api';

interface BoardMeta {
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  pinCount?: number;
  collaborators?: Collaborator[];
}

const BoardDetailPage: React.FC = () => {
  const { boardId } = useParams();
  const navigate = useNavigate();
  const staticBoard = boards.find((item) => item.id === boardId);
  const [boardPins, setBoardPins] = useState<Pin[]>([]);
  const [boardMeta, setBoardMeta] = useState<BoardMeta | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBoardData = async () => {
      if (!boardId) return;
      try {
        setLoading(true);
        setError(null);

        // Fetch board metadata
        const [boardRes, pinsRes] = await Promise.all([
          axios.get(`${API_BASE_URL}/boards/${boardId}`),
          axios.get(`${API_BASE_URL}/pins/board/${boardId}`),
        ]);

        const b = boardRes.data;
        setBoardMeta({
          name: b.name,
          description: b.description ?? '',
          createdAt: b.createdAt ?? new Date().toISOString(),
          updatedAt: b.updatedAt ?? new Date().toISOString(),
          pinCount: b.pinCount,
          collaborators: b.collaborators
            ? b.collaborators.map((c: any) => ({
                id: String(c.id),
                username: c.username,
                fullName: c.fullName,
                avatarUrl: c.avatarUrl,
              }))
            : [],
        });

        const backendPins = pinsRes.data as any[];
        const mapped: Pin[] = backendPins.map((p) => ({
          id: String(p.id),
          title: p.title,
          description: p.description ?? '',
          category: 'Uncategorized',
          imageUrl: p.mediaUrl,
          author: {
            name: p.ownerFullName || p.ownerUsername || 'Unknown',
          },
          board: p.boardName,
          stats: {
            saves: Number(p.saveCount ?? 0),
            shares: Number(p.shareCount ?? 0),
            likes: Number(p.likeCount ?? 0),
          },
          createdAt: p.createdAt ?? new Date().toISOString(),
          attribution: p.attribution,
          status: p.status,
        }));
        setBoardPins(mapped);
      } catch (err) {
        console.error('Failed to load board or pins:', err);
        setError('We could not load this board from the server.');
      } finally {
        setLoading(false);
      }
    };

    fetchBoardData();
  }, [boardId]);

  if (!staticBoard && !boardMeta) {
    return (
      <main className="container py-5">
        <p>We couldn't find that board. <Link to="/">Return home.</Link></p>
      </main>
    );
  }

  return (
    <main className="container py-4 py-md-5 board-detail-page">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>

      <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5 mb-5">
        <div className="row g-4 align-items-center">
          <div className="col-12 col-md-4">
            {staticBoard && (
              <div className="board-detail-cover rounded-4 overflow-hidden shadow-sm">
                <img
                  src={staticBoard.coverUrl}
                  alt={boardMeta?.name ?? staticBoard.title}
                  className="w-100 h-100 object-fit-cover"
                />
              </div>
            )}
          </div>
          <div className="col-12 col-md-8">
            <div className="mb-3">
              {staticBoard && (
                <span className="badge bg-body-secondary text-dark px-3 py-2 mb-2">
                  {staticBoard.category}
                </span>
              )}
              {staticBoard?.isFeatured && (
                <span className="badge bg-warning text-dark px-3 py-2 mb-2 ms-2 shadow-sm">⭐ Featured</span>
              )}
            </div>
            <h1 className="h2 mb-3 fw-bold">
              {boardMeta?.name ?? staticBoard?.title ?? 'Board'}
            </h1>
            <p className="lead mb-4 lh-base">
              {boardMeta?.description ?? staticBoard?.description ?? ''}
            </p>

            {boardMeta?.collaborators && boardMeta.collaborators.length > 0 && (
              <div className="mb-4">
                <h6 className="fw-semibold mb-3">Collaborators</h6>
                <div className="d-flex align-items-center gap-2 flex-wrap">
                  {boardMeta.collaborators.map((collab) => (
                    <div
                      key={collab.id}
                      className="d-flex align-items-center gap-2 bg-light rounded-pill px-3 py-2 shadow-sm"
                      title={`${collab.fullName} (@${collab.username})`}
                    >
                      <img
                        src={collab.avatarUrl}
                        alt={collab.fullName}
                        className="rounded-circle"
                        style={{ width: '28px', height: '28px', objectFit: 'cover' }}
                      />
                      <div className="d-flex flex-column">
                        <span className="small fw-medium">{collab.fullName}</span>
                        <span className="text-muted" style={{ fontSize: '0.7rem' }}>@{collab.username}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="d-flex flex-column flex-md-row gap-4 mb-4 p-3 bg-light bg-opacity-50 rounded-3">
              <div>
                <p className="text-muted small mb-1 fw-medium">Total Pins</p>
                <p className="h4 mb-0 fw-bold text-primary">
                  {boardMeta?.pinCount ?? boardPins.length}
                </p>
              </div>
              <div>
                <p className="text-muted small mb-1 fw-medium">Created</p>
                <p className="mb-0 fw-semibold">
                  {new Date(
                    boardMeta?.createdAt ?? staticBoard?.createdAt ?? new Date().toISOString()
                  ).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>
              <div>
                <p className="text-muted small mb-1 fw-medium">Last Updated</p>
                <p className="mb-0 fw-semibold">
                  {new Date(
                    boardMeta?.updatedAt ?? staticBoard?.updatedAt ?? new Date().toISOString()
                  ).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>
            </div>

            <div className="d-flex gap-2 flex-wrap">
              <button className="btn btn-dark rounded-pill px-4 shadow-sm">
                <span className="me-2">➕</span> Follow Board
              </button>
              <button className="btn btn-outline-secondary rounded-pill px-4 shadow-sm">
                <span className="me-2">📤</span> Share
              </button>
            </div>
          </div>
        </div>
      </section>

      {loading ? (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-0">Loading pins…</p>
        </section>
      ) : error ? (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-0">{error}</p>
        </section>
      ) : boardPins.length > 0 ? (
        <>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="h4 mb-0 fw-bold">All Pins ({boardPins.length})</h2>
          </div>
          <PinGrid pins={boardPins} />
        </>
      ) : (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-0">This board doesn't have any pins yet.</p>
        </section>
      )}
    </main>
  );
};

export default BoardDetailPage;

