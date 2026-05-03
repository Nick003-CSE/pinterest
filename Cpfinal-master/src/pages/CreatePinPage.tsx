import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
import { useSavedPins } from '../context/SavedPinsContext';
import { useAuth } from '../context/AuthContext';
import { Pin } from '../types/pin';

interface CreatePinForm {
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

const defaultForm: CreatePinForm = {
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

const CreatePinPage: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<CreatePinForm>(defaultForm);
  const [status, setStatus] = useState<string>('');
  const [isDraft, setIsDraft] = useState<boolean>(false);
  const [imagePreview, setImagePreview] = useState<string>('');
  const [imageFile, setImageFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { savePin } = useSavedPins();
  const { currentUser, backendUserId } = useAuth();
  const [userBoards, setUserBoards] = useState<{ id: string; name: string }[]>([]);

  useEffect(() => {
    const fetchBoards = async () => {
      if (!backendUserId) {
        setUserBoards([]);
        return;
      }
      try {
        const res = await axios.get(`${API_BASE_URL}/boards/owner/${backendUserId}`);
        const boards = res.data as any[];
        setUserBoards(
          boards.map((b) => ({
            id: String(b.id),
            name: b.name,
          }))
        );
      } catch (err) {
        console.error('Failed to load boards for user:', err);
        setUserBoards([]);
      }
    };

    fetchBoards();
  }, [backendUserId]);

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
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        setStatus('Please select a valid image file.');
        return;
      }
      
      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        setStatus('Image size must be less than 10MB.');
        return;
      }

      setImageFile(file);
      
      // Create preview using FileReader
      const reader = new FileReader();
      reader.onloadend = () => {
        const result = reader.result as string;
        setImagePreview(result);
        setForm((prev) => ({ ...prev, mediaUrl: result }));
      };
      reader.readAsDataURL(file);
      setStatus('');
    }
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

  const mapPinResponseToPin = (data: any): Pin => {
    return {
      id: String(data.id),
      ownerId: data.ownerId ? String(data.ownerId) : currentUser?.userId ? String(currentUser.userId) : undefined,
      ownerUsername: data.ownerUsername,
      title: data.title,
      description: data.description ?? '',
      category: form.category || 'Uncategorized',
      imageUrl: data.mediaUrl,
      board: data.boardName ?? form.newBoardName,
      author: {
        name: data.ownerFullName || data.ownerUsername || 'You',
      },
      stats: {
        saves: Number(data.saveCount ?? 0),
        shares: Number(data.shareCount ?? 0),
        likes: Number(data.likeCount ?? 0),
      },
      palette: [],
      keywords: data.keywords ?? buildKeywordsArray(),
      createdAt: data.createdAt ?? new Date().toISOString(),
      attribution: data.attribution ?? form.attribution,
    };
  };

  const submitPin = async (statusOverride: 'PUBLISHED' | 'DRAFT') => {
    if (!currentUser?.userId) {
      setStatus('Please sign in before creating a pin.');
      return;
    }

    try {
      const payload = {
        ownerId: currentUser.userId,
        boardId: form.boardId ? Number(form.boardId) : undefined,
        // Let backend either use existing board or create new one
        newBoardName: form.boardId ? undefined : form.newBoardName || undefined,
        title: form.title,
        description: form.description,
        mediaType: 'IMAGE',
        mediaUrl: form.mediaUrl || imagePreview,
        sourceUrl: form.sourceUrl,
        attribution: form.attribution,
        keywords: buildKeywordsArray(),
        visibility: form.visibility.toUpperCase(), // PUBLIC | PRIVATE
        status: statusOverride, // PUBLISHED | DRAFT
        mediaItems: [],
      };

      const response = await axios.post(`${API_BASE_URL}/pins`, payload);

      const createdPin = mapPinResponseToPin(response.data);
      savePin(createdPin);

      if (!form.boardId && response.data.boardId && response.data.boardName) {
        setUserBoards((prev) => [
          ...prev,
          { id: String(response.data.boardId), name: response.data.boardName },
        ]);
      }

      if (statusOverride === 'PUBLISHED') {
        setStatus('Pin published! View it on your profile.');
        setIsDraft(false);
        setTimeout(() => navigate('/', { replace: true }), 2000);
      } else {
        setStatus('Pin saved as draft. You can return anytime to finish it.');
        setIsDraft(true);
      }
    } catch (error) {
      console.error('Error creating pin:', error);
      setStatus('Failed to save pin. Please check your details and try again.');
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    await submitPin('PUBLISHED');
  };

  const handleDraft = async () => {
    await submitPin('DRAFT');
  };

  return (
    <main className="container py-4 py-md-5 create-pin-page">
      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-5">
            <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">✨ Create</p>
            <h1 className="h2 mb-4 fw-bold">Share a new Pin</h1>
            <form className="d-flex flex-column gap-3" onSubmit={handleSubmit}>
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
                    onChange={handleChange}
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
                      required
                    />
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
                  <select
                    name="category"
                    className="form-select"
                    value={form.category}
                    onChange={handleChange}
                  >
                    <option value="">Select a category</option>
                    <option value="Home & Decor">Home & Decor</option>
                    <option value="Wellness">Wellness</option>
                    <option value="Color Stories">Color Stories</option>
                    <option value="DIY">DIY</option>
                  </select>
                </div>
              </div>
              <div className="row g-3">
                <div className="col-12">
                  <label className="form-label">Image</label>
                  <div className="d-flex flex-column gap-2">
                    <div className="d-flex gap-2">
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        className="form-control"
                        onChange={handleImageFileChange}
                        style={{ display: 'none' }}
                        id="image-upload"
                      />
                      <label
                        htmlFor="image-upload"
                        className="btn btn-outline-secondary flex-grow-1"
                        style={{ cursor: 'pointer' }}
                      >
                        📷 Choose Image File
                      </label>
                      {imagePreview && (
                        <button
                          type="button"
                          className="btn btn-outline-danger"
                          onClick={handleRemoveImage}
                        >
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
                  <select
                    name="visibility"
                    className="form-select"
                    value={form.visibility}
                    onChange={handleChange}
                  >
                    <option value="public">Public</option>
                    <option value="private">Private</option>
                  </select>
                </div>
              </div>
              <div className="d-flex gap-3 flex-wrap">
                <button type="submit" className="btn btn-dark btn-lg rounded-pill px-4 shadow-sm">
                  📌 Publish
                </button>
                <button type="button" className="btn btn-outline-secondary btn-lg rounded-pill px-4" onClick={handleDraft}>
                  💾 Save as draft
                </button>
                {isDraft && (
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-lg rounded-pill"
                    onClick={() => setForm(defaultForm)}
                  >
                    Reset
                  </button>
                )}
              </div>
            </form>
            {status && <p className="mt-3 small">{status}</p>}
          </section>
        </div>
        <div className="col-12 col-lg-4">
          <section className="bg-white rounded-4 rounded-5 shadow-lg p-4 p-md-4 sticky-top" style={{ top: '100px' }}>
            <h2 className="h6 mb-3 fw-bold">Preview</h2>
            <article className="pin-card card border-0 rounded-4 shadow-sm overflow-hidden">
              <div className="pin-photo">
                {imagePreview || form.mediaUrl ? (
                  <img 
                    src={imagePreview || form.mediaUrl} 
                    alt={form.title || 'Preview'} 
                    className="w-100 h-100 object-fit-cover" 
                  />
                ) : (
                  <div className="placeholder d-flex align-items-center justify-content-center h-100 text-muted" style={{ minHeight: '300px' }}>
                    <div className="text-center">
                      <p className="mb-2">📷</p>
                      <p className="small">Upload an image or enter URL</p>
                    </div>
                  </div>
                )}
              </div>
              <div className="card-body">
                <span className="badge bg-body-secondary text-dark mb-2">{form.category || 'Category'}</span>
                <h3 className="h6">{form.title || 'Pin title'}</h3>
                <p className="text-muted small">{form.description || 'Pin description will appear here.'}</p>
              </div>
            </article>
          </section>
        </div>
      </div>
    </main>
  );
};

export default CreatePinPage;

