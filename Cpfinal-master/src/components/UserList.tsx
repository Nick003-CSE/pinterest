import React from 'react';
import { Link } from 'react-router-dom';
import { UserSummary } from '../types/user';

interface UserListProps {
  title: string;
  users: UserSummary[];
  actionLabel?: string;
  onViewAll?: () => void;
}

const UserList: React.FC<UserListProps> = ({ title, users, actionLabel, onViewAll }) => (
  <section className="user-list bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-4">
    <div className="d-flex justify-content-between align-items-center mb-4">
      <h3 className="h6 mb-0 fw-bold">{title}</h3>
      {actionLabel && (
        <button className="btn btn-link btn-sm p-0 text-decoration-none fw-medium" onClick={onViewAll}>
          {actionLabel} →
        </button>
      )}
    </div>
    <ul className="list-unstyled m-0 d-flex flex-column gap-3">
      {users.slice(0, 5).map((user) => (
        <li key={user.id} className="d-flex align-items-center justify-content-between gap-3 p-2 rounded-3 bg-light bg-opacity-50">
          <Link
            to={`/user/${user.id}`}
            className="d-flex align-items-center gap-3 text-decoration-none text-reset flex-grow-1"
          >
            <img src={user.avatarUrl} alt={user.name} className="rounded-circle avatar-sm shadow-sm" />
            <div>
              <p className="mb-0 fw-semibold small">{user.name}</p>
              <small className="text-muted d-block">{user.username}</small>
              {user.role && <small className="text-muted d-block">{user.role}</small>}
            </div>
          </Link>
          <Link
            to={`/user/${user.id}`}
            className="btn btn-outline-secondary btn-sm rounded-pill px-3 shadow-sm"
          >
            View
          </Link>
        </li>
      ))}
      {users.length === 0 && (
        <li className="text-muted small text-center py-3">No users yet</li>
      )}
    </ul>
  </section>
);

export default UserList;

