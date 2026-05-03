import React from 'react';
import { Link } from 'react-router-dom';
import { useInvitations } from '../context/InvitationContext';
import { useUser } from '../context/UserContext';
import { Invitation } from '../types/invitation';

const InvitationsPanel: React.FC = () => {
  const { getPendingInvitations, acceptInvitation, declineInvitation, ignoreInvitation } = useInvitations();
  const { addToFollowing } = useUser();
  const pendingInvitations = getPendingInvitations().slice(0, 3); // Show only first 3

  const handleAccept = async (invitation: Invitation) => {
    const result = await acceptInvitation(invitation.id);

    if (result.success) {
      // Handle connection invitation
      if (invitation.type === 'Connection' && invitation.inviterId) {
        const inviterUser = {
          id: invitation.inviterId,
          name: invitation.inviterName,
          username: `@${invitation.inviterName.toLowerCase().replace(/\s+/g, '')}`,
          avatarUrl: invitation.inviterAvatar,
        };
        await addToFollowing(inviterUser);
      }
      // Board collaboration invitations will be surfaced in the collaborations panel
    }
  };

  const handleDecline = async (invitation: Invitation) => {
    await declineInvitation(invitation.id);
  };

  const handleIgnore = async (invitation: Invitation) => {
    await ignoreInvitation(invitation.id);
  };

  const visibleInvitations = pendingInvitations;

  const allPendingCount = getPendingInvitations().length;

  if (visibleInvitations.length === 0 && allPendingCount === 0) {
    return (
      <section className="invitations-panel bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-4 mt-4">
        <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">✉️ Invitations</p>
        <p className="text-muted small mb-0">No pending invitations</p>
      </section>
    );
  }

  return (
    <section className="invitations-panel bg-white rounded-4 rounded-5 shadow-sm p-4 p-md-4 mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <p className="text-uppercase small text-muted mb-1 fw-semibold letter-spacing-1">✉️ Invitations</p>
          <h2 className="h6 mb-0 fw-bold">Collaborate & connect</h2>
        </div>
        {allPendingCount > 0 && (
          <span className="badge bg-danger rounded-pill shadow-sm px-3 py-2">{allPendingCount}</span>
        )}
      </div>
      <div className="d-flex flex-column gap-3">
        {visibleInvitations.map((invite) => (
          <article key={invite.id} className="invitation-card d-flex gap-3">
            <Link
              to={`/user/${invite.inviterId || 'unknown'}`}
              className="flex-shrink-0"
            >
              <img
                src={invite.inviterAvatar}
                alt={invite.inviterName}
                className="rounded-circle avatar-sm"
              />
            </Link>
            <div className="flex-grow-1">
              <Link
                to={`/user/${invite.inviterId || 'unknown'}`}
                className="text-decoration-none text-reset"
              >
                <p className="mb-0 fw-semibold">{invite.inviterName}</p>
              </Link>
              <small className="text-muted d-block">
                {invite.type}
                {invite.boardTitle ? ` • ${invite.boardTitle}` : ''}
              </small>
              <p className="small mt-1 mb-2">{invite.message}</p>
              <div className="d-flex gap-2 flex-wrap">
                <button
                  className="btn btn-dark btn-sm rounded-pill px-3 shadow-sm"
                  onClick={() => handleAccept(invite)}
                >
                  ✓ Accept
                </button>
                <button
                  className="btn btn-outline-secondary btn-sm rounded-pill px-3"
                  onClick={() => handleDecline(invite)}
                >
                  ✕ Decline
                </button>
                <button
                  className="btn btn-link btn-sm p-0 text-muted text-decoration-none"
                  onClick={() => handleIgnore(invite)}
                  title="Ignore"
                >
                  Ignore
                </button>
              </div>
            </div>
          </article>
        ))}
        {allPendingCount > visibleInvitations.length && (
          <Link
            to="/invitations"
            className="btn btn-outline-secondary btn-sm rounded-pill text-center"
          >
            View all {allPendingCount} invitations
          </Link>
        )}
        {visibleInvitations.length === 0 && (
          <p className="text-muted small text-center py-3">No pending invitations</p>
        )}
      </div>
    </section>
  );
};

export default InvitationsPanel;

