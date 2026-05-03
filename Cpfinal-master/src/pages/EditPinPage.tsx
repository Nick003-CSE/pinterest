import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { useSavedPins } from '../context/SavedPinsContext';
import { useAuth } from '../context/AuthContext';
import { Pin } from '../types/pin';
import { API_BASE_URL } from '../config/api';

interface EditPinForm {
  title: string;
  description: string;
  category: string;
  boardId: string;
  newBoardName: string;
  mediaUrl: string;
  sourceUrl: string;
  visibility: 'public' | 'private';
  keywords: string;
  attribution: string;
}

const defaultForm: EditPinForm = {
  title: '',
  description: '',
  category: '',
  boardId: '',
  newBoardName: '',
  mediaUrl: '',
  sourceUrl: '',
  visibility: 'public',
  keywords: '',
  attribution: '',
};

const EditPinPage: React.FC = () => {
  const { pinId } = useParams<{ pinId: string }>();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { currentUser, backendUserId } = useAuth();
  const { refreshPins } = useSavedPins();

  const [form, setForm] = useState<EditPinForm>(defaultForm);
  const [status, setStatus] = useState<string>('');
  const [isDraft, setIsDraft] = useState<boolean>(false);
  const [imagePreview, setImagePreview] = useState<string>('');
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [userBoards, setUserBoards] = useState<{ id: string; name: string }[]>([]);
  const [loadingPin, setLoadingPin] = useState<boolean>(true);
  const [detachFromBoard, setDetachFromBoard] = useState<boolean>(false);
  const [currentBoardName, setCurrentBoardName] = useState<string | null>(null);

  useEffect(() => {
    const fetchBoards = async () => {
      if (!backendUserId) {
        setUserBoards([]);
        return;
      }
      try {
        const res = await axios.get(`${API_BASE_URL}/boards/owner/${backendUserId}`);
        const boards = Array.isArray(res.data) ? res.data : [];
        setUserBoards(
          boards.map((b: any) => ({
            id: String(b.id),
            name: b.name,
          }))
        );
      } catch (error) {
        console.error('Failed to load boards for editing:', error);
        setUserBoards([]);
      }
    };

    fetchBoards();
  }, [backendUserId]);

  useEffect(() => {
    const fetchPin = async () => {
      if (!pinId) {
        setStatus('Invalid pin selected for editing.');
        return;
      }
      try {
        setLoadingPin(true);
        const response = await axios.get(`${API_BASE_URL}/pins/${pinId}`);
        const pin = response.data;

        if (currentUser?.userId && pin.ownerId && Number(pin.ownerId) !== Number(currentUser.userId)) {
          setStatus('You can only edit pins you created.');
          navigate(`/pin/${pinId}`, { replace: true });
          return;
        }

        setForm({
          title: pin.title || '',
          description: pin.description || '',
          category: pin.boardName || 'Uncategorized',
          boardId: pin.boardId ? String(pin.boardId) : '',
          newBoardName: '',
          mediaUrl: pin.mediaUrl || '',
          sourceUrl: pin.sourceUrl || '',
          visibility: (pin.visibility || 'PUBLIC').toLowerCase() === 'private' ? 'private' : 'public',
          keywords: Array.isArray(pin.keywords) ? pin.keywords.join(', ') : '',
          attribution: pin.attribution || '',
        });
        setImagePreview(pin.mediaUrl || '');
        setCurrentBoardName(pin.boardName || null);
        setIsDraft(pin.status === 'DRAFT');
        setDetachFromBoard(false);
        setStatus('');
      } catch (error) {
        console.error('Failed to load pin for editing:', error);
        setStatus('Unable to load this pin for editing.');
      } finally {
        setLoadingPin(false);
      }
    };

    if (currentUser?.userId) {
      fetchPin();
    }
  }, [pinId, currentUser?.userId, navigate]);

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = event.target;
    setForm((prev) => {
      if (name === 'boardId') {
        return {
          ...prev,
          boardId: value,
          newBoardName: value === '' ? prev.newBoardName : '',
        };
      }
      return { ...prev, [name]: value };
    });
  };

  const handleImageFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setStatus('Please select a valid image file.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setStatus('Image size must be less than 10MB.');
      return;
    }
    setImageFile(file);
    const reader = new FileReader();
    reader.onloadend = () => {
      const result = reader.result as string;
      setImagePreview(result);
      setForm((prev) => ({ ...prev, mediaUrl: result }));
    };
    reader.readAsDataURL(file);
    setStatus('');
  };

  const handleRemoveImage = () => {
    setImageFile(null);
    setImagePreview('');
    setForm((prev) => ({ ...prev, mediaUrl: '' }));
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const buildKeywordsArray = () => {
    if (!form.keywords.trim()) return [];
    return form.keywords
      .split(',')
      .map((k) => k.trim())
      .filter((k) => k.length > 0);
  };

  const mapPinResponseToPin = (data: any): Pin => ({
    id: String(data.id),
    ownerId: data.ownerId ? String(data.ownerId) : currentUser?.userId ? String(currentUser.userId) : undefined,
    ownerUsername: data.ownerUsername,
    title: data.title,
    description: data.description ?? '',
    category: data.boardName || form.category || 'Uncategorized',
    imageUrl: data.mediaUrl,
    board: data.boardName,
    author: {
      name: data.ownerFullName || data.ownerUsername || 'You',
    },
    stats: {
      saves: Number(data.saveCount ?? 0),
      shares: Number(data.shareCount ?? 0),
      likes: Number(data.likeCount ?? 0),
    },
    keywords: data.keywords ?? buildKeywordsArray(),
    createdAt: data.createdAt ?? new Date().toISOString(),
    attribution: data.attribution ?? form.attribution,
    status: data.status,
  });

  const submitUpdate = async (statusOverride: 'PUBLISHED' | 'DRAFT') => {
    if (!pinId || !currentUser?.userId) {
      setStatus('Please sign in before editing a pin.');
      return;
    }

    try {
      const payload = {
        ownerId: currentUser.userId,
        boardId: form.boardId ? Number(form.boardId) : undefined,
        newBoardName: form.boardId ? undefined : form.newBoardName || undefined,
        detachFromBoard,
        title: form.title,
        description: form.description,
        mediaType: 'IMAGE',
        mediaUrl: form.mediaUrl || imagePreview,
        sourceUrl: form.sourceUrl,
        attribution: form.attribution,
        keywords: buildKeywordsArray(),
        visibility: form.visibility.toUpperCase(),
        status: statusOverride,
        mediaItems: [],
      };

      const response = await axios.put(`${API_BASE_URL}/pins/${pinId}`, payload);
      const updatedPin = mapPinResponseToPin(response.data);
      await refreshPins();

      setStatus('Pin updated successfully!');
      setTimeout(() => {
        navigate(`/pin/${pinId}`, { replace: true, state: { pin: updatedPin } });
      }, 1200);
    } catch (error) {
      console.error('Failed to update pin:', error);
      setStatus('We could not save your changes. Please try again.');
    }
  };

  if (loadingPin) {
    return (
      <main className="container py-5 text-center">
        <p className="text-muted">Loading pin details…</p>
      </main>
    );
  }

  if (!pinId) {
    return (
      <main className="container py-5 text-center">
        <p className="text-muted">Invalid pin selected.</p>
      </main>
    );
  }

  return (
    <main className="container py-4 py-md-5 create-pin-page">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm mb-4 rounded-pill px-3 shadow-sm"
        onClick={() => navigate(-1)}
      >
        ← Back
      </button>
      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5">
            <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">✏️ Edit</p>
            <h1 className="h2 mb-4 fw-bold">Update your Pin</h1>
            <form
              className="d-flex flex-column gap-3"
              onSubmit={(event) => {
                event.preventDefault();
                submitUpdate('PUBLISHED');
              }}
            >
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label">Title</label>
                  <input
                    name="title"
                    className="form-control form-control-lg"
                    value={form.title}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label">Board</label>
                  <select
                    name="boardId"
                    className="form-select form-select-lg"
                    value={form.boardId}
                    onChange={(event) => {
                      setDetachFromBoard(false);
                      handleChange(event);
                    }}
                  >
                    <option value="">Create new board…</option>
                    {userBoards.map((b) => (
                      <option key={b.id} value={b.id}>
                        {b.name}
                      </option>
                    ))}
                  </select>
                  {form.boardId === '' && (
                    <input
                      name="newBoardName"
                      className="form-control form-control-lg mt-2"
                      value={form.newBoardName}
                      onChange={handleChange}
                      placeholder="New board name"
                      required={!detachFromBoard}
                    />
                  )}
                  {currentBoardName && (
                    <div className="form-check mt-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id="detach-board"
                        checked={detachFromBoard}
                        onChange={(event) => {
                          setDetachFromBoard(event.target.checked);
                          if (event.target.checked) {
                            setForm((prev) => ({ ...prev, boardId: '', newBoardName: '' }));
                          }
                        }}
                      />
                      <label className="form-check-label" htmlFor="detach-board">
                        Remove from "{currentBoardName}"
                      </label>
                    </div>
                  )}
                </div>
              </div>
              <div>
                <label className="form-label">Description</label>
                <textarea
                  name="description"
                  className="form-control"
                  rows={3}
                  value={form.description}
                  onChange={handleChange}
                />
              </div>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label">Keywords</label>
                  <input
                    name="keywords"
                    className="form-control"
                    value={form.keywords}
                    onChange={handleChange}
                    placeholder="mindful, neutral palette"
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label">Category</label>
                  <input
                    name="category"
                    className="form-control"
                    value={form.category}
                    onChange={handleChange}
                    placeholder="Home & Decor"
                  />
                </div>
              </div>
              <div className="row g-3">
                <div className="col-12">
                  <label className="form-label">Image</label>
                  <div className="d-flex flex-column gap-2">
                    <div className="d-flex gap-2 flex-wrap">
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        className="form-control"
                        onChange={handleImageFileChange}
                        style={{ display: 'none' }}
                        id="edit-image-upload"
                      />
                      <label
                        htmlFor="edit-image-upload"
                        className="btn btn-outline-secondary flex-grow-1"
                        style={{ cursor: 'pointer' }}
                      >
                        📷 Choose Image File
                      </label>
                      {imagePreview && (
                        <button type="button" className="btn btn-outline-danger" onClick={handleRemoveImage}>
                          Remove
                        </button>
                      )}
                    </div>
                    {imagePreview && (
                      <div className="mt-2">
                        <img
                          src={imagePreview}
                          alt="Preview"
                          className="img-thumbnail"
                          style={{ maxHeight: '200px', maxWidth: '100%', objectFit: 'contain' }}
                        />
                        {imageFile && (
                          <p className="small text-muted mt-2 mb-0">
                            Selected: {imageFile.name} ({(imageFile.size / 1024 / 1024).toFixed(2)} MB)
                          </p>
                        )}
                      </div>
                    )}
                    <div className="text-center text-muted small my-2">OR</div>
                    <input
                      name="mediaUrl"
                      className="form-control"
                      value={form.mediaUrl && !imagePreview ? form.mediaUrl : ''}
                      onChange={handleChange}
                      placeholder="Enter image URL (https://...)"
                      disabled={!!imagePreview}
                    />
                    {imagePreview && (
                      <small className="text-muted">Remove uploaded image to use URL instead</small>
                    )}
                  </div>
                </div>
              </div>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label">Source / attribution URL</label>
                  <input
                    name="sourceUrl"
                    className="form-control"
                    value={form.sourceUrl}
                    onChange={handleChange}
                    placeholder="https://"
                  />
                </div>
              </div>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label">Attribution notes</label>
                  <input
                    name="attribution"
                    className="form-control"
                    value={form.attribution}
                    onChange={handleChange}
                    placeholder="Photo by..."
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label">Visibility</label>
                  <select name="visibility" className="form-select" value={form.visibility} onChange={handleChange}>
                    <option value="public">Public</option>
                    <option value="private">Private</option>
                  </select>
                </div>
              </div>
              <div className="d-flex gap-3 flex-wrap">
                <button type="submit" className="btn btn-dark btn-lg rounded-pill px-4 shadow-sm">
                  💾 Save Changes
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-lg rounded-pill px-4"
                  onClick={() => submitUpdate('DRAFT')}
                >
                  📝 Save as draft
                </button>
                {isDraft && (
                  <button
                    type="button"
                    className="btn btn-outline-success btn-lg rounded-pill px-4"
                    onClick={() => submitUpdate('PUBLISHED')}
                  >
                    📣 Publish now
                  </button>
                )}
              </div>
            </form>
            {status && <div className="alert alert-info mt-4">{status}</div>}
          </section>
        </div>
      </div>
    </main>
  );
};

export default EditPinPage;

