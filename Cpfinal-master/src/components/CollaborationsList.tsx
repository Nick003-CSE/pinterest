import React from 'react';
import { Link } from 'react-router-dom';
import { useCollaborations } from '../context/CollaborationContext';

const CollaborationsList: React.FC = () => {
  const { collaborations, getCollaborators } = useCollaborations();
  const collaborators = getCollaborators();

  // Group collaborations by user
  const collaborationsByUser = collaborators.map((collaborator) => {
    const userCollaborations = collaborations.filter((collab) => collab.userId === collaborator.id);
    return {
      collaborator,
      boards: userCollaborations.map((collab) => collab.boardTitle),
    };
  });

  return (
    <section className="collaborations-list bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="h6 mb-0 fw-bold">🤝 Collaborations</h3>
        <span className="badge bg-primary rounded-pill shadow-sm px-3 py-2">{collaborators.length}</span>
      </div>
      {collaborationsByUser.length > 0 ? (
        <ul className="list-unstyled m-0 d-flex flex-column gap-3">
          {collaborationsByUser.map(({ collaborator, boards }) => (
            <li key={collaborator.id} className="d-flex align-items-start gap-3 p-2 rounded-3 bg-light bg-opacity-50">
              <Link
                to={`/user/${collaborator.id}`}
                className="flex-shrink-0"
              >
                <img
                  src={collaborator.avatarUrl}
                  alt={collaborator.name}
                  className="rounded-circle avatar-sm shadow-sm"
                />
              </Link>
              <div className="flex-grow-1">
                <Link
                  to={`/user/${collaborator.id}`}
                  className="text-decoration-none text-reset"
                >
                  <p className="mb-0 fw-semibold small">{collaborator.name}</p>
                </Link>
                <small className="text-muted d-block">{collaborator.username}</small>
                <div className="mt-2">
                  <small className="text-muted d-block mb-1 fw-medium">Collaborating on:</small>
                  <div className="d-flex flex-wrap gap-1">
                    {boards.map((boardTitle, index) => (
                      <span
                        key={index}
                        className="badge bg-body-secondary text-dark rounded-pill px-2 py-1"
                      >
                        {boardTitle}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
              <Link
                to={`/user/${collaborator.id}`}
                className="btn btn-outline-secondary btn-sm rounded-pill px-3 shadow-sm"
              >
                View
              </Link>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted small text-center py-3 mb-0">
          No active collaborations. Accept board collaboration invitations to start collaborating!
        </p>
      )}
    </section>
  );
};

export default CollaborationsList;

