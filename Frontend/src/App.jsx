import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Vehicles from './pages/Vehicles';
import VehicleDetails from './pages/VehicleDetails';
import Orders from './pages/Orders';
import AddVehicle from './pages/AddVehicle';

// Protected Route Wrapper for Authenticated Users
const PrivateRoute = ({ children }) => {
  const { token, loading } = useAuth();
  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
      <div className="glow-effect" style={{ padding: '1rem 2rem', borderRadius: '12px', background: 'rgba(255,255,255,0.03)' }}>
        Authenticating...
      </div>
    </div>
  );
  return token ? children : <Navigate to="/login" />;
};

// Admin Only Route Wrapper
const AdminRoute = ({ children }) => {
  const { token, role, loading } = useAuth();
  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
      <div className="glow-effect" style={{ padding: '1rem 2rem', borderRadius: '12px', background: 'rgba(255,255,255,0.03)' }}>
        Authenticating...
      </div>
    </div>
  );
  return token && role === 'ADMIN' ? children : <Navigate to="/vehicles" />;
};

function AppContent() {
  const { user } = useAuth();
  const [toasts, setToasts] = useState([]);

  const addToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  };

  return (
    <Router>
      <div className="app-container">
        <Navbar />
        
        <main className="main-content">
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<Login addToast={addToast} />} />
            <Route path="/register" element={<Register addToast={addToast} />} />

            {/* Protected Routes */}
            <Route path="/vehicles" element={
              <PrivateRoute>
                <Vehicles addToast={addToast} />
              </PrivateRoute>
            } />
            
            <Route path="/vehicles/:id" element={
              <PrivateRoute>
                <VehicleDetails addToast={addToast} />
              </PrivateRoute>
            } />
            
            <Route path="/orders" element={
              <PrivateRoute>
                <Orders addToast={addToast} />
              </PrivateRoute>
            } />

            {/* Admin-only Routes */}
            <Route path="/add-vehicle" element={
              <AdminRoute>
                <AddVehicle addToast={addToast} />
              </AdminRoute>
            } />

            {/* Default Route redirect */}
            <Route path="*" element={
              user ? <Navigate to="/vehicles" replace /> : <Navigate to="/login" replace />
            } />
          </Routes>
        </main>

        <footer style={{ borderTop: '1px solid var(--glass-border)', padding: '2rem 1.5rem', textAlign: 'center', fontSize: '0.88rem', color: '#6b7280', marginTop: 'auto', background: 'rgba(3,7,18,0.5)' }}>
          <div>&copy; {new Date().getFullYear()} DriveLux Luxury Inventory Management. All rights reserved.</div>
          <div style={{ marginTop: '0.25rem' }}>Designed with custom Glassmorphic CSS in React.</div>
        </footer>

        {/* Floating Toast Notification Container */}
        <div className="toast-container">
          {toasts.map((t) => (
            <div key={t.id} className={`toast toast-${t.type}`}>
              <span>{t.message}</span>
            </div>
          ))}
        </div>
      </div>
    </Router>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
