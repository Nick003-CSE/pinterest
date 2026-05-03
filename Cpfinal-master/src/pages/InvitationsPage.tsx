import React, { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useInvitations } from '../context/InvitationContext';
import { useUser } from '../context/UserContext';
import { Invitation } from '../types/invitation';
import { UserSummary } from '../types/user';

const InvitationsPage: React.FC = () => {
  const navigate = useNavigate();
  const { invitationId } = useParams<{ invitationId?: string }>();
  const { invitations, acceptInvitation, declineInvitation, ignoreInvitation, getPendingInvitations } =
    useInvitations();
  const { addToFollowing } = useUser();
  const [sortOption, setSortOption] = useState<'recent' | 'oldest' | 'type'>('recent');
  const [filterType, setFilterType] = useState<'all' | 'Board Collaboration' | 'Connection'>('all');
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const pendingInvitations = getPendingInvitations();

  const filteredAndSortedInvitations = useMemo(() => {
    let result = [...pendingInvitations];

    // Filter by type
    if (filterType !== 'all') {
      result = result.filter((inv) => inv.type === filterType);
    }

    // Sort
    if (sortOption === 'recent') {
      result.sort((a, b) => {
        const timeA = new Date(a.sentAt).getTime() || 0;
        const timeB = new Date(b.sentAt).getTime() || 0;
        return timeB - timeA;
      });
    } else if (sortOption === 'oldest') {
      result.sort((a, b) => {
        const timeA = new Date(a.sentAt).getTime() || 0;
        const timeB = new Date(b.sentAt).getTime() || 0;
        return timeA - timeB;
      });
    } else if (sortOption === 'type') {
      result.sort((a, b) => a.type.localeCompare(b.type));
    }

    return result;
  }, [pendingInvitations, sortOption, filterType]);

  const selectedInvitation = invitationId
    ? invitations.find((inv) => inv.id === invitationId)
    : null;

  const handleAccept = async (invitation: Invitation) => {
    const result = await acceptInvitation(invitation.id);

    if (result.success) {
      // Handle board collaboration
      if (invitation.type === 'Board Collaboration' && invitation.boardTitle) {
        // In a real app, this would add the board to the user's boards via API
        // For now, we'll show a success message
        setStatusMessage({
          type: 'success',
          text: `Board "${invitation.boardTitle}" has been added to your profile! You are now a collaborator.`,
        });
      }

      // Handle connection invitation
      if (invitation.type === 'Connection' && invitation.inviterId) {
        // Add inviter to following list
        const inviterUser: UserSummary = {
          id: invitation.inviterId,
          name: invitation.inviterName,
          username: `@${invitation.inviterName.toLowerCase().replace(/\s+/g, '')}`,
          avatarUrl: invitation.inviterAvatar,
        };
        await addToFollowing(inviterUser);
        setStatusMessage({ type: 'success', text: `You are now connected with ${invitation.inviterName}!` });
      }

      // Clear message after 3 seconds
      setTimeout(() => setStatusMessage(null), 3000);
    } else {
      setStatusMessage({ type: 'error', text: result.error || 'Failed to accept invitation' });
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  const handleDecline = async (invitation: Invitation) => {
    const result = await declineInvitation(invitation.id);

    if (result.success) {
      setStatusMessage({ type: 'success', text: 'Invitation declined' });
      setTimeout(() => setStatusMessage(null), 3000);
    } else {
      setStatusMessage({ type: 'error', text: result.error || 'Failed to decline invitation' });
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  const handleIgnore = async (invitation: Invitation) => {
    const result = await ignoreInvitation(invitation.id);

    if (result.success) {
      setStatusMessage({ type: 'success', text: 'Invitation ignored' });
      setTimeout(() => setStatusMessage(null), 3000);
    } else {
      setStatusMessage({ type: 'error', text: result.error || 'Failed to ignore invitation' });
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  if (selectedInvitation) {
    return (
      <main className="container py-4 py-md-5">
        <button
          type="button"
          className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
          onClick={() => navigate('/invitations')}
        >
          ← Back to Invitations
        </button>

        <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5">
          <div className="row g-4">
            <div className="col-12 col-md-4 text-center text-md-start">
              <Link to={`/user/${selectedInvitation.inviterId || 'unknown'}`}>
                <img
                  src={selectedInvitation.inviterAvatar}
                  alt={selectedInvitation.inviterName}
                  className="rounded-circle mb-3"
                  style={{ width: '120px', height: '120px', objectFit: 'cover' }}
                />
              </Link>
              <h3 className="h5 mb-1">
                <Link
                  to={`/user/${selectedInvitation.inviterId || 'unknown'}`}
                  className="text-decoration-none text-reset"
                >
                  {selectedInvitation.inviterName}
                </Link>
              </h3>
              <p className="text-muted small mb-0">
                Sent {new Date(selectedInvitation.sentAt).toLocaleString()}
              </p>
            </div>
            <div className="col-12 col-md-8">
              <div className="mb-3">
                <span className="badge bg-body-secondary text-dark mb-2">{selectedInvitation.type}</span>
                {selectedInvitation.boardTitle && (
                  <h2 className="h4 mb-2">{selectedInvitation.boardTitle}</h2>
                )}
                <p className="mb-3">{selectedInvitation.message}</p>
              </div>

              {statusMessage && (
                <div
                  className={`alert alert-${statusMessage.type === 'success' ? 'success' : 'danger'} mb-3`}
                  role="alert"
                >
                  {statusMessage.text}
                </div>
              )}

              <div className="d-flex gap-2 flex-wrap">
                <button
                  className="btn btn-dark rounded-pill"
                  onClick={() => handleAccept(selectedInvitation)}
                  disabled={selectedInvitation.status !== 'pending'}
                >
                  Accept
                </button>
                <button
                  className="btn btn-outline-secondary rounded-pill"
                  onClick={() => handleDecline(selectedInvitation)}
                  disabled={selectedInvitation.status !== 'pending'}
                >
                  Decline
                </button>
                <button
                  className="btn btn-outline-secondary rounded-pill"
                  onClick={() => handleIgnore(selectedInvitation)}
                  disabled={selectedInvitation.status !== 'pending'}
                >
                  Ignore
                </button>
                <Link
                  to={`/user/${selectedInvitation.inviterId || 'unknown'}`}
                  className="btn btn-outline-secondary rounded-pill"
                >
                  View Profile
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="container py-4 py-md-5">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4 mb-md-5">
        <div>
          <h1 className="h2 mb-1 fw-bold">✉️ Invitations</h1>
          <p className="text-muted small mb-0">
            {filteredAndSortedInvitations.length} pending invitation{filteredAndSortedInvitations.length !== 1 ? 's' : ''}
          </p>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <select
            className="form-select w-auto shadow-sm"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value as any)}
          >
            <option value="all">All types</option>
            <option value="Board Collaboration">Board Collaboration</option>
            <option value="Connection">Connection</option>
          </select>
          <select
            className="form-select w-auto shadow-sm"
            value={sortOption}
            onChange={(e) => setSortOption(e.target.value as any)}
          >
            <option value="recent">Most recent</option>
            <option value="oldest">Oldest first</option>
            <option value="type">By type</option>
          </select>
        </div>
      </div>

      {statusMessage && (
        <div
          className={`alert alert-${statusMessage.type === 'success' ? 'success' : 'danger'} mb-4 rounded-4 shadow-sm`}
          role="alert"
        >
          {statusMessage.text}
        </div>
      )}

      {filteredAndSortedInvitations.length > 0 ? (
        <div className="d-flex flex-column gap-3">
          {filteredAndSortedInvitations.map((invitation) => (
            <article
              key={invitation.id}
              className="invitation-card bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-4"
            >
              <div className="d-flex gap-3">
                <Link
                  to={`/user/${invitation.inviterId || 'unknown'}`}
                  className="flex-shrink-0"
                >
                  <img
                    src={invitation.inviterAvatar}
                    alt={invitation.inviterName}
                    className="rounded-circle"
                    style={{ width: '64px', height: '64px', objectFit: 'cover' }}
                  />
                </Link>
                <div className="flex-grow-1">
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <div>
                      <Link
                        to={`/user/${invitation.inviterId || 'unknown'}`}
                        className="text-decoration-none text-reset"
                      >
                        <h3 className="h6 mb-0 fw-semibold">{invitation.inviterName}</h3>
                      </Link>
                      <small className="text-muted d-block">
                        {invitation.type}
                        {invitation.boardTitle ? ` • ${invitation.boardTitle}` : ''}
                      </small>
                      <small className="text-muted d-block">
                        Sent {new Date(invitation.sentAt).toLocaleString()}
                      </small>
                    </div>
                    <Link
                      to={`/invitations/${invitation.id}`}
                      className="btn btn-outline-secondary btn-sm rounded-pill px-3 shadow-sm"
                    >
                      View Details
                    </Link>
                  </div>
                  <p className="small mb-3 lh-sm">{invitation.message}</p>
                  <div className="d-flex gap-2 flex-wrap">
                    <button
                      className="btn btn-dark btn-sm rounded-pill px-3 shadow-sm"
                      onClick={() => handleAccept(invitation)}
                    >
                      ✓ Accept
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill px-3"
                      onClick={() => handleDecline(invitation)}
                    >
                      ✕ Decline
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-sm rounded-pill px-3"
                      onClick={() => handleIgnore(invitation)}
                    >
                      Ignore
                    </button>
                  </div>
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <section className="bg-white rounded-4 rounded-5 shadow-lg p-5 text-center">
          <p className="text-muted mb-3 fw-medium">No pending invitations</p>
          <p className="small text-muted mb-0">You're all caught up! Check back later for new invitations.</p>
        </section>
      )}
    </main>
  );
};

export default InvitationsPage;

