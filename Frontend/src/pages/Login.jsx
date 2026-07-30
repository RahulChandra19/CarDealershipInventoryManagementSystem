import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogIn } from 'lucide-react';

const Login = ({ addToast }) => {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  // Show session expired message if redirected
  const sessionExpired = searchParams.get('expired');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!username || !password) {
      setError('Please fill in all fields');
      return;
    }

    const result = await login(username, password);
    if (result.success) {
      addToast('Logged in successfully!', 'success');
      navigate('/vehicles');
    } else {
      setError(result.message);
      addToast(result.message, 'error');
    }
  };

  return (
    <div className="auth-page animate-fade-in">
      <div className="auth-card glass-panel">
        <div className="auth-header">
          <h1 className="auth-title">Welcome Back</h1>
          <p className="auth-subtitle">Log in to manage your luxury fleet</p>
        </div>

        {sessionExpired && (
          <div className="badge badge-warning" style={{ width: '100%', padding: '0.75rem', marginBottom: '1.25rem', justifyContent: 'center' }}>
            Your session has expired. Please log in again.
          </div>
        )}

        {error && (
          <div className="badge badge-error" style={{ width: '100%', padding: '0.75rem', marginBottom: '1.25rem', justifyContent: 'center' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} id="login-form">
          <div className="form-group">
            <label className="form-label" htmlFor="login-username">Username</label>
            <input
              type="text"
              id="login-username"
              className="form-input"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="login-password">Password</label>
            <input
              type="password"
              id="login-password"
              className="form-input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '1rem' }}
            disabled={loading}
          >
            <LogIn size={18} /> {loading ? 'Logging in...' : 'Log In'}
          </button>
        </form>

        <div className="auth-footer">
          Don't have an account?{' '}
          <Link to="/register" className="auth-link">Sign Up</Link>
        </div>

        {/* <div className="demo-credentials glass-panel" style={{ marginTop: '2rem', padding: '1rem', fontSize: '0.85rem', background: 'rgba(255, 255, 255, 0.02)' }}>
          <p style={{ fontWeight: '600', marginBottom: '0.25rem', color: 'white' }}>🔑 Development Seed Accounts:</p>
          <p><strong>Admin:</strong> admin / admin12345</p>
          <p><strong>Customer:</strong> customer / password123</p>
        </div> */}
      </div>
    </div>
  );
};

export default Login;
