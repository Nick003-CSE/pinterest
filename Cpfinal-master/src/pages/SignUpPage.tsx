import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';

interface SignUpForm {
  fullName: string;
  email: string;
  username: string;
  phoneNumber: string;
  password: string;
  confirmPassword: string;
}

const usernameRegex = /^[a-z0-9._-]{3,16}$/;
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,16}$/;
// Indian phone number: 10 digits starting with 6-9
const phoneRegex = /^[6-9]\d{9}$/;

const SignUpPage: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<SignUpForm>({
    fullName: '',
    email: '',
    username: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<Partial<SignUpForm>>({});
  const [status, setStatus] = useState<string>('');

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    if (name === 'phoneNumber') {
      const digitsOnly = value.replace(/\D/g, '').substring(0, 10);
      setForm((prev) => ({ ...prev, [name]: digitsOnly }));
    } else {
      setForm((prev) => ({ ...prev, [name]: value }));
    }
  };

  const validate = () => {
    const newErrors: Partial<SignUpForm> = {};
    if (!form.fullName.trim()) {
      newErrors.fullName = 'Your name is required';
    }
    if (!form.email.match(/^[^@]+@[^@]+\.[^@]+$/)) {
      newErrors.email = 'Provide a valid email address';
    }
    if (!usernameRegex.test(form.username)) {
      newErrors.username = 'Use 3-16 lowercase letters, digits, or ._-';
    }
    if (!form.phoneNumber.trim()) {
      newErrors.phoneNumber = 'Phone number is required';
    } else {
      const cleanedPhone = form.phoneNumber.replace(/\s/g, '').replace(/-/g, '');
      if (!phoneRegex.test(cleanedPhone)) {
        newErrors.phoneNumber = 'Please enter a valid 10-digit Indian phone number';
      }
    }
    if (!passwordRegex.test(form.password)) {
      newErrors.password =
        '8-16 chars with lowercase, uppercase, number, and special character.';
    }
    if (form.password !== form.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await axios.post(`${API_BASE_URL}/auth/register`, {
        email: form.email,
        username: form.username,
        fullName: form.fullName,
        phoneNumber: form.phoneNumber,
        password: form.password,
        confirmPassword: form.confirmPassword,
      });

      console.log('Signup Success:', response.data);
      const successMessage = 'Signup successful! Please login.';
      setStatus(successMessage);
      alert(successMessage);
      navigate('/signin');
    } catch (error) {
      console.error('Signup failed:', error);
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        setStatus(error.response.data.message);
        alert(error.response.data.message);
      } else {
        const failureMessage = 'Signup failed! Please try again.';
        setStatus(failureMessage);
        alert(failureMessage);
      }
    }
  };

  return (
    <main className="auth-page container py-5">
      <div className="auth-layout shadow-lg">
        <section className="auth-form-panel">
          <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">Create an account</p>
          <h1 className="h2 mb-2 fw-bold">Join Framescape</h1>
          <p className="small mb-4">
            Already have an account?{' '}
            <button className="btn btn-link p-0" onClick={() => navigate('/signin')}>
              Sign in
            </button>
          </p>
          <form className="d-flex flex-column gap-3" onSubmit={handleSubmit}>
            <div>
              <label className="form-label">Full name</label>
              <input
                type="text"
                name="fullName"
                className="form-control form-control-lg"
                value={form.fullName}
                onChange={handleChange}
                required
              />
              {errors.fullName && <small className="text-danger">{errors.fullName}</small>}
            </div>
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
              <label className="form-label">Username</label>
              <input
                type="text"
                name="username"
                className="form-control form-control-lg"
                value={form.username}
                onChange={handleChange}
                required
              />
              {errors.username && <small className="text-danger">{errors.username}</small>}
            </div>
            <div>
              <label className="form-label">Phone Number</label>
              <input
                type="tel"
                name="phoneNumber"
                className="form-control form-control-lg"
                value={form.phoneNumber}
                onChange={handleChange}
                placeholder="9876543210"
                maxLength={10}
                required
              />
              <small className="text-muted d-block mt-1">Enter your 10-digit mobile number</small>
              {errors.phoneNumber && <small className="text-danger d-block">{errors.phoneNumber}</small>}
            </div>
            <div className="row g-3">
              <div className="col-12 col-md-6">
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
              <div className="col-12 col-md-6">
                <label className="form-label">Confirm password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  className="form-control form-control-lg"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  required
                />
                {errors.confirmPassword && (
                  <small className="text-danger">{errors.confirmPassword}</small>
                )}
              </div>
            </div>
            <button type="submit" className="btn btn-dark btn-lg rounded-pill w-100 shadow-sm">
              🚀 Sign up
            </button>
          </form>
          {status && <p className="mt-3 small">{status}</p>}
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

export default SignUpPage;

