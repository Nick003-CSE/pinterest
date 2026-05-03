import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../config/api';

type Step = 'phone' | 'otp' | 'password';

const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>('phone');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [status, setStatus] = useState('');
  const [otpTimer, setOtpTimer] = useState(0);
  const [generatedOtp, setGeneratedOtp] = useState('');
  const otpInputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // OTP Timer countdown
  useEffect(() => {
    if (otpTimer > 0) {
      const timer = setTimeout(() => setOtpTimer(otpTimer - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [otpTimer]);

  // Auto-focus next OTP input
  useEffect(() => {
    if (step === 'otp' && otpInputRefs.current[0]) {
      otpInputRefs.current[0]?.focus();
    }
  }, [step]);

  const handlePhoneChange = (value: string) => {
    const digitsOnly = value.replace(/\D/g, '');
    const limitedDigits = digitsOnly.substring(0, 10);
    setPhoneNumber(limitedDigits);
    setErrors({});
  };

  const generateOTP = (): string => {
    // Generate 6-digit OTP
    return Math.floor(100000 + Math.random() * 900000).toString();
  };

  const handleSendOTP = async () => {
    setErrors({});
    setStatus('');

    if (!phoneNumber || phoneNumber.length !== 10) {
      setErrors({ phoneNumber: 'Please enter a valid 10-digit phone number' });
      return;
    }

    try {
      const response = await axios.post<{
        message: string;
        otp: string;
      }>(`${API_BASE_URL}/auth/forgot-password/request-otp`, {
        phoneNumber,
      });

      const otpCode = response.data.otp;
      setGeneratedOtp(otpCode);
      setOtpTimer(60); // 60 seconds timer
      setStep('otp');
      setStatus(
        `${response.data.message} OTP sent to +91 ${phoneNumber}. For demo purposes, your OTP is: ${otpCode}`
      );
    } catch (error: any) {
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        setErrors({ phoneNumber: error.response.data.message });
      } else {
        setErrors({ phoneNumber: 'Unable to send OTP. Please try again.' });
      }
    }
  };

  const handleOtpChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return; // Only allow digits

    const newOtp = [...otp];
    newOtp[index] = value.substring(0, 1);
    setOtp(newOtp);
    setErrors({});

    // Auto-focus next input
    if (value && index < 5) {
      otpInputRefs.current[index + 1]?.focus();
    }

    // Auto-verify when all 6 digits are entered
    if (newOtp.every((digit) => digit !== '') && newOtp.join('').length === 6) {
      setTimeout(() => handleVerifyOTP(newOtp.join('')), 100);
    }
  };

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      otpInputRefs.current[index - 1]?.focus();
    }
  };

  const handleVerifyOTP = async (otpValue?: string) => {
    const otpToVerify = otpValue || otp.join('');
    setErrors({});
    setStatus('');

    if (otpToVerify.length !== 6) {
      setErrors({ otp: 'Please enter the complete 6-digit OTP' });
      return;
    }

    if (otpToVerify !== generatedOtp) {
      setErrors({ otp: 'Invalid OTP. Please try again.' });
      return;
    }

    try {
      await axios.post(`${API_BASE_URL}/auth/forgot-password/verify-otp`, {
        phoneNumber,
        otp: otpToVerify,
      });
      setStep('password');
      setStatus('OTP verified successfully! Please enter your new password.');
    } catch (error: any) {
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        setErrors({ otp: error.response.data.message });
      } else {
        setErrors({ otp: 'Failed to verify OTP. Please try again.' });
      }
    }
  };

  const handleResendOTP = () => {
    if (otpTimer > 0) return;

    const otpCode = generateOTP();
    setGeneratedOtp(otpCode);
    setOtpTimer(60);
    setOtp(['', '', '', '', '', '']);
    setStatus(`New OTP sent! For demo purposes, your OTP is: ${otpCode}`);
    otpInputRefs.current[0]?.focus();
  };

  const handlePasswordChange = (name: string, value: string) => {
    if (name === 'newPassword') {
      setNewPassword(value);
    } else {
      setConfirmPassword(value);
    }
    setErrors({});
  };

  const validatePassword = () => {
    const newErrors: Record<string, string> = {};
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,16}$/;

    if (!newPassword) {
      newErrors.newPassword = 'New password is required';
    } else if (!passwordRegex.test(newPassword)) {
      newErrors.newPassword =
        'Password must be 8-16 chars with lowercase, uppercase, number, and special character.';
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (newPassword !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleResetPassword = async () => {
    if (!validatePassword()) return;

    try {
      await axios.post(`${API_BASE_URL}/auth/forgot-password/reset`, {
        phoneNumber,
        otp: otp.join(''),
        newPassword,
        confirmPassword,
      });
      setStatus('Password reset successfully! Redirecting to sign in...');
      setTimeout(() => navigate('/signin'), 2000);
    } catch (error: any) {
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        setErrors({ general: error.response.data.message });
      } else {
        setErrors({ general: 'Failed to reset password. Please try again.' });
      }
    }
  };

  return (
    <main className="auth-page container py-5">
      <div className="auth-layout shadow-lg">
        <section className="auth-form-panel">
          <button
            type="button"
            className="btn btn-link p-0 mb-3 text-decoration-none"
            onClick={() => navigate('/signin')}
          >
            ← Back to Sign In
          </button>
          <p className="text-uppercase small text-muted mb-2 fw-semibold letter-spacing-1">Reset Password</p>
          <h1 className="h2 mb-4 fw-bold">Forgot Password?</h1>

          {/* Step Indicator */}
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div className={`step-indicator ${step === 'phone' ? 'active' : step === 'otp' || step === 'password' ? 'completed' : ''}`}>
              <span className="step-number">1</span>
              <span className="step-label d-none d-md-inline">Phone</span>
            </div>
            <div className={`step-line ${step === 'otp' || step === 'password' ? 'completed' : ''}`}></div>
            <div className={`step-indicator ${step === 'otp' ? 'active' : step === 'password' ? 'completed' : ''}`}>
              <span className="step-number">2</span>
              <span className="step-label d-none d-md-inline">OTP</span>
            </div>
            <div className={`step-line ${step === 'password' ? 'completed' : ''}`}></div>
            <div className={`step-indicator ${step === 'password' ? 'active' : ''}`}>
              <span className="step-number">3</span>
              <span className="step-label d-none d-md-inline">Password</span>
            </div>
          </div>

          {/* Step 1: Phone Number */}
          {step === 'phone' && (
            <div className="d-flex flex-column gap-3">
              <div>
                <label className="form-label">Phone Number</label>
                <div className="input-group">
                  <span className="input-group-text bg-light fw-semibold">🇮🇳 +91</span>
                  <input
                    type="tel"
                    className="form-control form-control-lg"
                    value={phoneNumber}
                    onChange={(e) => handlePhoneChange(e.target.value)}
                    placeholder="9876543210"
                    maxLength={10}
                    required
                  />
                </div>
                <small className="text-muted d-block mt-1">Enter your registered 10-digit mobile number</small>
                {errors.phoneNumber && <small className="text-danger d-block">{errors.phoneNumber}</small>}
              </div>
              <button
                type="button"
                className="btn btn-dark btn-lg rounded-pill w-100 shadow-sm"
                onClick={handleSendOTP}
              >
                Send OTP
              </button>
            </div>
          )}

          {/* Step 2: OTP Verification */}
          {step === 'otp' && (
            <div className="d-flex flex-column gap-3">
              <div>
                <label className="form-label">Enter OTP</label>
                <p className="small text-muted mb-3">
                  We've sent a 6-digit OTP to <strong>+91 {phoneNumber}</strong>
                </p>
                <div className="d-flex justify-content-center gap-2 mb-3">
                  {otp.map((digit, index) => (
                    <input
                      key={index}
                      ref={(el) => {
                        otpInputRefs.current[index] = el;
                      }}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      className="form-control form-control-lg text-center"
                      style={{ width: '50px', fontSize: '1.5rem', fontWeight: 'bold' }}
                      value={digit}
                      onChange={(e) => handleOtpChange(index, e.target.value)}
                      onKeyDown={(e) => handleOtpKeyDown(index, e)}
                    />
                  ))}
                </div>
                {errors.otp && <small className="text-danger d-block text-center">{errors.otp}</small>}
                <div className="text-center">
                  {otpTimer > 0 ? (
                    <small className="text-muted">
                      Resend OTP in <strong>{otpTimer}s</strong>
                    </small>
                  ) : (
                    <button
                      type="button"
                      className="btn btn-link p-0 text-decoration-none"
                      onClick={handleResendOTP}
                    >
                      Resend OTP
                    </button>
                  )}
                </div>
              </div>
              <button
                type="button"
                className="btn btn-dark btn-lg rounded-pill w-100 shadow-sm"
                onClick={() => handleVerifyOTP()}
                disabled={otp.join('').length !== 6}
              >
                Verify OTP
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary rounded-pill w-100"
                onClick={() => {
                  setStep('phone');
                  setOtp(['', '', '', '', '', '']);
                  setErrors({});
                }}
              >
                Change Phone Number
              </button>
            </div>
          )}

          {/* Step 3: New Password */}
          {step === 'password' && (
            <div className="d-flex flex-column gap-3">
              <div>
                <label className="form-label">New Password</label>
                <input
                  type="password"
                  name="newPassword"
                  className="form-control form-control-lg"
                  value={newPassword}
                  onChange={(e) => handlePasswordChange('newPassword', e.target.value)}
                  placeholder="Enter new password"
                  required
                />
                {errors.newPassword && <small className="text-danger">{errors.newPassword}</small>}
                <small className="text-muted d-block mt-1">
                  8-16 chars with lowercase, uppercase, number, and special character
                </small>
              </div>
              <div>
                <label className="form-label">Confirm New Password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  className="form-control form-control-lg"
                  value={confirmPassword}
                  onChange={(e) => handlePasswordChange('confirmPassword', e.target.value)}
                  placeholder="Confirm new password"
                  required
                />
                {errors.confirmPassword && <small className="text-danger">{errors.confirmPassword}</small>}
              </div>
              {errors.general && <div className="alert alert-danger">{errors.general}</div>}
              <button
                type="button"
                className="btn btn-dark btn-lg rounded-pill w-100 shadow-sm"
                onClick={handleResetPassword}
              >
                Reset Password
              </button>
            </div>
          )}

          {status && (
            <div className={`alert ${status.includes('successfully') || status.includes('sent') ? 'alert-success' : 'alert-info'} mt-3`}>
              {status}
            </div>
          )}
        </section>
        <aside className="auth-side-panel">
          <div className="auth-quote text-center text-md-start">
            <p className="lead fw-semibold mb-4">
              "Security is our top priority. Your account is protected with OTP verification."
            </p>
            <p className="mb-0">
              — Framescape Security Team
              <br />
              <span className="text-muted">Keeping your account safe</span>
            </p>
          </div>
        </aside>
      </div>
    </main>
  );
};

export default ForgotPasswordPage;

