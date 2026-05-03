import React, { useState } from 'react';
import { UserSummary } from '../types/user';

interface CollaborationInviteModalProps {
  isOpen: boolean;
  onClose: () => void;
  following: UserSummary[];
  boardTitle: string;
  onInvite: (selectedUserIds: string[]) => Promise<void>;
}

const CollaborationInviteModal: React.FC<CollaborationInviteModalProps> = ({
  isOpen,
  onClose,
  following,
  boardTitle,
  onInvite,
}) => {
  const [selectedUsers, setSelectedUsers] = useState<Set<string>>(new Set());
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleToggleUser = (userId: string) => {
    setSelectedUsers((prev) => {
      const next = new Set(prev);
      if (next.has(userId)) {
        next.delete(userId);
      } else {
        next.add(userId);
      }
      return next;
    });
  };

  const handleSelectAll = () => {
    if (selectedUsers.size === following.length) {
      setSelectedUsers(new Set());
    } else {
      setSelectedUsers(new Set(following.map((u) => u.id)));
    }
  };

  const handleSubmit = async () => {
    if (selectedUsers.size === 0) {
      alert('Please select at least one person to invite.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onInvite(Array.from(selectedUsers));
      setSelectedUsers(new Set());
      onClose();
    } catch (error) {
      console.error('Failed to send invitations:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setSelectedUsers(new Set());
      onClose();
    }
  };

  return (
    <div
      className="modal show d-block"
      tabIndex={-1}
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)' }}
      onClick={handleClose}
    >
      <div
        className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title fw-bold">
              Invite Collaborators to &quot;{boardTitle}&quot;
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={handleClose}
              disabled={isSubmitting}
              aria-label="Close"
            />
          </div>
          <div className="modal-body">
            {following.length === 0 ? (
              <div className="text-center py-4">
                <p className="text-muted">
                  You are not following anyone yet. Follow people to invite them to collaborate.
                </p>
              </div>
            ) : (
              <>
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <p className="mb-0 text-muted small">
                    Select people from your following list to invite
                  </p>
                  <button
                    type="button"
                    className="btn btn-link btn-sm p-0 text-decoration-none"
                    onClick={handleSelectAll}
                  >
                    {selectedUsers.size === following.length ? 'Deselect All' : 'Select All'}
                  </button>
                </div>
                <div className="border rounded-3" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  <ul className="list-unstyled m-0">
                    {following.map((user) => {
                      const isSelected = selectedUsers.has(user.id);
                      return (
                        <li
                          key={user.id}
                          className={`p-3 border-bottom d-flex align-items-center gap-3 cursor-pointer ${
                            isSelected ? 'bg-light' : ''
                          }`}
                          style={{ cursor: 'pointer' }}
                          onClick={() => handleToggleUser(user.id)}
                        >
                          <div className="form-check">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              checked={isSelected}
                              onChange={() => handleToggleUser(user.id)}
                              id={`user-${user.id}`}
                            />
                          </div>
                          <img
                            src={user.avatarUrl}
                            alt={user.name}
                            className="rounded-circle"
                            style={{ width: '40px', height: '40px', objectFit: 'cover' }}
                          />
                          <div className="flex-grow-1">
                            <p className="mb-0 fw-semibold small">{user.name}</p>
                            <small className="text-muted">@{user.username}</small>
                          </div>
                          {isSelected && (
                            <span className="badge bg-primary rounded-pill">Selected</span>
                          )}
                        </li>
                      );
                    })}
                  </ul>
                </div>
                <div className="mt-3">
                  <small className="text-muted">
                    {selectedUsers.size} of {following.length} selected
                  </small>
                </div>
              </>
            )}
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSubmit}
              disabled={isSubmitting || selectedUsers.size === 0}
            >
              {isSubmitting ? (
                <>
                  <span
                    className="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  />
                  Sending...
                </>
              ) : (
                `Send Invitations (${selectedUsers.size})`
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CollaborationInviteModal;

