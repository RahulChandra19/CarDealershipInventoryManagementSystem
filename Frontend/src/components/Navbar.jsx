import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Car, LogOut, ShoppingCart, PlusCircle, User, Menu, X } from 'lucide-react';

const Navbar = () => {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar-container">
      <div className="navbar-content">
        <Link to="/" className="navbar-logo" onClick={() => setMobileMenuOpen(false)}>
          <Car size={32} className="logo-icon" />
          <span>DRIVELUX</span>
        </Link>

        {/* Desktop Navigation */}
        {user ? (
          <div className="navbar-links">
            <Link to="/vehicles" className={`nav-link ${isActive('/vehicles') ? 'active' : ''}`}>
              Vehicles
            </Link>
            
            {role === 'ADMIN' ? (
              <>
                <Link to="/add-vehicle" className={`nav-link ${isActive('/add-vehicle') ? 'active' : ''}`}>
                  <PlusCircle size={18} /> Add Vehicle
                </Link>
                <Link to="/orders" className={`nav-link ${isActive('/orders') ? 'active' : ''}`}>
                  <ShoppingCart size={18} /> All Orders
                </Link>
              </>
            ) : (
              <Link to="/orders" className={`nav-link ${isActive('/orders') ? 'active' : ''}`}>
                <ShoppingCart size={18} /> My Orders
              </Link>
            )}

            <div className="user-profile-badge">
              <User size={16} />
              <span className="username-text">{user.username}</span>
              <span className={`badge ${role === 'ADMIN' ? 'badge-admin' : 'badge-customer'}`}>
                {role}
              </span>
            </div>

            <button onClick={handleLogout} className="btn-logout" title="Log Out">
              <LogOut size={20} />
            </button>
          </div>
        ) : (
          <div className="navbar-links">
            <Link to="/login" className="btn btn-secondary">Log In</Link>
            <Link to="/register" className="btn btn-primary">Sign Up</Link>
          </div>
        )}

        {/* Mobile menu toggle */}
        {user && (
          <button className="mobile-toggle" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
            {mobileMenuOpen ? <X size={26} /> : <Menu size={26} />}
          </button>
        )}
      </div>

      {/* Mobile Menu */}
      {user && mobileMenuOpen && (
        <div className="mobile-menu glass-panel">
          <Link to="/vehicles" className={`mobile-nav-link ${isActive('/vehicles') ? 'active' : ''}`} onClick={() => setMobileMenuOpen(false)}>
            Vehicles
          </Link>
          {role === 'ADMIN' ? (
            <>
              <Link to="/add-vehicle" className={`mobile-nav-link ${isActive('/add-vehicle') ? 'active' : ''}`} onClick={() => setMobileMenuOpen(false)}>
                Add Vehicle
              </Link>
              <Link to="/orders" className={`mobile-nav-link ${isActive('/orders') ? 'active' : ''}`} onClick={() => setMobileMenuOpen(false)}>
                All Orders
              </Link>
            </>
          ) : (
            <Link to="/orders" className={`mobile-nav-link ${isActive('/orders') ? 'active' : ''}`} onClick={() => setMobileMenuOpen(false)}>
              My Orders
            </Link>
          )}
          <div className="mobile-profile">
            <User size={18} />
            <span>{user.username}</span>
            <span className={`badge ${role === 'ADMIN' ? 'badge-admin' : 'badge-customer'}`}>{role}</span>
          </div>
          <button onClick={handleLogout} className="btn btn-danger mobile-btn-logout">
            <LogOut size={18} /> Log Out
          </button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
