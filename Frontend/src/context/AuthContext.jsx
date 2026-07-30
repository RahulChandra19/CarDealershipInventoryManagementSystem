import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [role, setRole] = useState(localStorage.getItem('role') || null);
  const [loading, setLoading] = useState(true);

  // Verifies current user profile on app load
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const response = await api.get('/auth/me');
          setUser(response.data);
          setRole(response.data.role);
          localStorage.setItem('role', response.data.role);
        } catch (error) {
          console.error('Invalid token or session expired', error);
          logout();
        }
      }
      setLoading(false);
    };
    initAuth();
  }, [token]);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { username, password });
      const { token, role: userRole } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('role', userRole);
      setToken(token);
      setRole(userRole);
      
      // Immediately fetch user info to set context
      const meResponse = await api.get('/auth/me');
      setUser(meResponse.data);
      setLoading(false);
      return { success: true };
    } catch (error) {
      setLoading(false);
      const errMsg = error.response?.data?.message || 'Login failed. Please check credentials.';
      return { success: false, message: errMsg };
    }
  };

  const register = async (username, email, password) => {
    setLoading(true);
    try {
      const response = await api.post('/auth/register', { username, email, password });
      const { token, role: userRole } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('role', userRole);
      setToken(token);
      setRole(userRole);
      
      const meResponse = await api.get('/auth/me');
      setUser(meResponse.data);
      setLoading(false);
      return { success: true };
    } catch (error) {
      setLoading(false);
      const errMsg = error.response?.data?.message || 'Registration failed. Try a different username/email.';
      return { success: false, message: errMsg };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setToken(null);
    setRole(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, role, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
