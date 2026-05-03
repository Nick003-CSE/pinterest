import React from 'react';
import { Link } from 'react-router-dom';
import { Board } from '../types/board';

interface BoardCardProps {
  board: Board;
  actions?: React.ReactNode;
}

const BoardCard: React.FC<BoardCardProps> = ({ board, actions }) => (
  <div className="board-card-wrapper">
    <Link to={`/board/${board.id}`} className="text-decoration-none text-reset d-block mb-2">
      <article className="board-card card border-0 rounded-4 rounded-5 shadow-sm overflow-hidden h-100 transition-all">
        <div className="board-cover position-relative">
          <img src={board.coverUrl} alt={board.title} className="w-100 h-100 object-fit-cover" />
          {board.isFeatured && (
            <span className="badge bg-warning text-dark featured-pill shadow-sm px-3 py-2">
              ⭐ Featured
            </span>
          )}
        </div>
        <div className="card-body p-3 p-md-4">
          <p className="text-uppercase text-muted small mb-2 fw-semibold">{board.category}</p>
          <h3 className="h5 mb-2 fw-bold">{board.title}</h3>
          <p className="text-muted small mb-3 lh-sm">{board.description}</p>
          {board.collaborators && board.collaborators.length > 0 && (
            <div className="mb-3">
              <div className="d-flex align-items-center gap-2 flex-wrap">
                <span className="text-muted small fw-semibold">Collaborators:</span>
                <div className="d-flex align-items-center gap-1" style={{ marginLeft: '-4px' }}>
                  {board.collaborators.slice(0, 3).map((collab, index) => (
                    <img
                      key={collab.id}
                      src={collab.avatarUrl}
                      alt={collab.fullName}
                      className="rounded-circle border border-2 border-white"
                      style={{
                        width: '24px',
                        height: '24px',
                        objectFit: 'cover',
                        marginLeft: index > 0 ? '-8px' : '0',
                        zIndex: 3 - index,
                        position: 'relative',
                      }}
                      title={collab.fullName}
                    />
                  ))}
                  {board.collaborators.length > 3 && (
                    <span className="small text-muted ms-1 fw-medium">+{board.collaborators.length - 3}</span>
                  )}
                </div>
              </div>
            </div>
          )}
          <div className="d-flex align-items-center justify-content-between small pt-2 border-top">
            <span className="fw-semibold">{board.pinIds.length} pins</span>
            <span className="text-muted">{board.updatedAt}</span>
          </div>
        </div>
      </article>
    </Link>
    {actions && <div className="d-flex gap-2 flex-wrap mt-2">{actions}</div>}
  </div>
);

export default BoardCard;

