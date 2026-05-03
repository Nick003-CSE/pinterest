import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface SignInForm {
  email: string;
  password: string;
}

const SignInPage: React.FC = () => {
  const navigate = useNavigate();
  const { signIn } = useAuth();
  const [form, setForm] = useState<SignInForm>({ email: '', password: '' });
  const [errors, setErrors] = useState<Partial<SignInForm>>({});
  const [status, setStatus] = useState<string>('');
  const [failedAttempts, setFailedAttempts] = useState<number>(0);
  const [lockoutExpiry, setLockoutExpiry] = useState<number | null>(null);

  const isLocked = lockoutExpiry !== null && Date.now() < lockoutExpiry;
  const lockoutSeconds = useMemo(() => {
    if (!lockoutExpiry) return 0;
    return Math.max(0, Math.ceil((lockoutExpiry - Date.now()) / 1000));
  }, [lockoutExpiry]);

  useEffect(() => {
    if (!lockoutExpiry) return;
    const timer = setInterval(() => {
      if (Date.now() >= lockoutExpiry) {
        setLockoutExpiry(null);
        setFailedAttempts(0);
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [lockoutExpiry]);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const newErrors: Partial<SignInForm> = {};
    if (!form.email.includes('@')) {
      newErrors.email = 'Enter a valid email';
    }
    if (form.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (isLocked) {
      setStatus('Login temporarily disabled. Please wait for the timer to finish.');
      return;
    }
    if (!validate()) {
      return;
    }
    const result = await signIn(form.email, form.password);
    if (result.ok) {
      setStatus(result.message || 'Success! Redirecting to your home feed…');
      setTimeout(() => navigate('/'), 500);
      return;
    }
    const attempts = failedAttempts + 1;
    setFailedAttempts(attempts);
    setStatus(result.message || 'Invalid credentials. Please try again.');
    if (attempts >= 3) {
      setLockoutExpiry(Date.now() + 60 * 1000);
    }
  };

  return (
    <main className="auth-page container py-5">
      <div className="auth-layout shadow-lg">
        <section className="auth-form-panel">
          <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">Welcome back</p>
          <h1 className="h2 mb-4 fw-bold">Sign in to Framescape</h1>
          <form className="d-flex flex-column gap-3" onSubmit={handleSubmit}>
            <div>
              <label className="form-label">Email address</label>
              <input
                type="email"
                name="email"
                className="form-control form-control-lg"
                value={form.email}
                onChange={handleChange}
                required
              />
              {errors.email && <small className="text-danger">{errors.email}</small>}
            </div>
            <div>
              <label className="form-label">Password</label>
              <input
                type="password"
                name="password"
                className="form-control form-control-lg"
                value={form.password}
                onChange={handleChange}
                required
              />
              {errors.password && <small className="text-danger">{errors.password}</small>}
            </div>
            <div className="d-flex justify-content-between align-items-center">
              <div className="form-check">
                <input className="form-check-input" type="checkbox" id="rememberMe" />
                <label className="form-check-label" htmlFor="rememberMe">
                  Remember me
                </label>
              </div>
              <button
                type="button"
                className="btn btn-link p-0 text-decoration-none"
                onClick={() => navigate('/forgot-password')}
              >
                Forgot password?
              </button>
            </div>
            <button type="submit" className="btn btn-dark btn-lg rounded-pill w-100 shadow-sm" disabled={isLocked}>
              {isLocked ? `🔒 Locked (${lockoutSeconds}s)` : '🚀 Sign in'}
            </button>
          </form>
          {status && <p className="mt-3 small">{status}</p>}
          <p className="mt-4 small text-muted">
            Don’t have an account?{' '}
            <button className="btn btn-link p-0" onClick={() => navigate('/signup')}>
              Create one
            </button>
          </p>
        </section>
        <aside className="auth-side-panel">
          <div className="auth-quote text-center text-md-start">
            <p className="lead fw-semibold mb-4">
              “The customer service I received was exceptional. The support team went above and
              beyond to address my concerns.”
            </p>
            <p className="mb-0">
              — Jules Winfield
              <br />
              <span className="text-muted">CEO, Acme Corp</span>
            </p>
          </div>
        </aside>
      </div>
    </main>
  );
};

export default SignInPage;

