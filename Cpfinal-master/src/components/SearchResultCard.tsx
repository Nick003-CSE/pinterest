import React from 'react';
import { Link } from 'react-router-dom';
import { Pin } from '../types/pin';
import { Board } from '../types/board';

type ResultItem = (Pin | Board) & { resultType: 'Pin' | 'Board' };

interface SearchResultCardProps {
  item: ResultItem;
}

const SearchResultCard: React.FC<SearchResultCardProps> = ({ item }) => {
  const isPin = item.resultType === 'Pin';
  const pin = isPin ? (item as Pin) : null;
  const board = !isPin ? (item as Board) : null;
  
  const thumbnail = isPin ? pin!.imageUrl : board!.coverUrl;
  const title = item.title;
  const description = 'description' in item ? item.description : '';
  const creator = isPin ? pin!.author.name : 'Board';
  const creatorLocation = isPin ? pin!.author.location : null;
  const creatorAvatar = isPin ? pin!.author.avatarUrl : null;

  return (
    <article className="search-result-card card border-0 rounded-4 rounded-5 shadow-sm overflow-hidden h-100">
      <Link
        to={isPin ? `/pin/${item.id}` : `/board/${item.id}`}
        className="text-decoration-none text-reset"
        state={isPin ? { pin } : undefined}
      >
        <div className="result-thumbnail position-relative">
          <img src={thumbnail} alt={title} className="w-100 h-100 object-fit-cover" />
          <span className="badge bg-light text-dark position-absolute top-0 start-0 m-2 shadow-sm px-3 py-2">
            {item.resultType}
          </span>
        </div>
        <div className="card-body d-flex flex-column p-3 p-md-4">
          <div className="mb-2">
            <span className="badge bg-body-secondary text-dark small px-2 py-1">
              {isPin ? pin!.category : board!.category}
            </span>
          </div>
          <h3 className="h5 mb-2 fw-bold">{title}</h3>
          <p className="text-muted small mb-3 flex-grow-1 lh-sm">{description}</p>
          
          <div className="d-flex align-items-center gap-2 mb-3">
            {creatorAvatar && (
              <img
                src={creatorAvatar}
                alt={creator}
                className="rounded-circle"
                style={{ width: '32px', height: '32px', objectFit: 'cover' }}
              />
            )}
            <div className="flex-grow-1">
              <p className="mb-0 small fw-semibold">{creator}</p>
              {creatorLocation && (
                <p className="mb-0 text-muted" style={{ fontSize: '0.75rem' }}>
                  {creatorLocation}
                </p>
              )}
            </div>
          </div>

          {isPin && (
            <div className="d-flex gap-3 small text-muted mb-3">
              <span>{pin!.stats.saves.toLocaleString()} saves</span>
              <span>{pin!.stats.shares.toLocaleString()} shares</span>
            </div>
          )}

          {!isPin && (
            <div className="small text-muted mb-3">
              {board!.pinIds.length} pins
            </div>
          )}
        </div>
      </Link>
    </article>
  );
};

export default SearchResultCard;

