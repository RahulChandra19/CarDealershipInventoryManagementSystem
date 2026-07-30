import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { PlusCircle, ArrowLeft } from 'lucide-react';

const AddVehicle = ({ addToast }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    make: '',
    model: '',
    year: new Date().getFullYear(),
    category: 'Electric',
    price: '',
    quantity: 1,
    vin: '',
    imageUrl: '',
    description: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await api.post('/vehicles', {
        ...form,
        price: parseFloat(form.price),
        quantity: parseInt(form.quantity),
        year: parseInt(form.year)
      });
      addToast(`${form.make} ${form.model} added to inventory successfully!`, 'success');
      navigate(`/vehicles/${response.data.id}`);
    } catch (error) {
      console.error(error);
      const msg = error.response?.data?.message || 'Failed to add vehicle to inventory.';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="animate-fade-in">
      <button onClick={() => navigate('/vehicles')} className="btn btn-secondary" style={{ marginBottom: '1.5rem' }}>
        <ArrowLeft size={16} /> Cancel
      </button>

      <div className="form-panel glass-panel">
        <div style={{ borderBottom: '1px solid var(--glass-border)', paddingBottom: '1rem', marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.8rem', background: 'linear-gradient(to right, #ffffff, #a855f7)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Register New Vehicle
          </h2>
          <p style={{ color: '#9ca3af', fontSize: '0.9rem', marginTop: '0.25rem' }}>Enter specifications to catalog a premium vehicle</p>
        </div>

        <form onSubmit={handleSubmit} id="add-vehicle-form">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label" htmlFor="make">Make *</label>
              <input
                type="text"
                id="make"
                name="make"
                className="form-input"
                placeholder="e.g. Tesla"
                value={form.make}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="model">Model *</label>
              <input
                type="text"
                id="model"
                name="model"
                className="form-input"
                placeholder="e.g. Model S"
                value={form.model}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="year">Year *</label>
              <input
                type="number"
                id="year"
                name="year"
                className="form-input"
                min="1900"
                max={new Date().getFullYear() + 2}
                value={form.year}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>

            {/* <div className="form-group">
              <label className="form-label" htmlFor="category">Category *</label>
              <select
                id="category"
                name="category"
                className="form-input"
                value={form.category}
                onChange={handleChange}
                required
                disabled={loading}
              >
                <option value="Electric">Electric</option>
                <option value="Sports">Sports</option>
                <option value="Truck">Truck</option>
                <option value="SUV">SUV</option>
                <option value="Sedan">Sedan</option>
              </select>
            </div> */}

            <div className="form-group">
  <label className="form-label" htmlFor="category">
    Category *
  </label>

  <select
    id="category"
    name="category"
    className="form-input category-select"
    value={form.category}
    onChange={handleChange}
    required
    disabled={loading}
  >
    <option value="Electric">Electric</option>
    <option value="Sports">Sports</option>
    <option value="Truck">Truck</option>
    <option value="SUV">SUV</option>
    <option value="Sedan">Sedan</option>
  </select>
</div>

            <div className="form-group">
              <label className="form-label" htmlFor="price">Price ($) *</label>
              <input
                type="number"
                id="price"
                name="price"
                className="form-input"
                placeholder="e.g. 89900"
                min="0"
                step="0.01"
                value={form.price}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="quantity">Initial Stock *</label>
              <input
                type="number"
                id="quantity"
                name="quantity"
                className="form-input"
                min="0"
                value={form.quantity}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group span-2">
              <label className="form-label" htmlFor="vin">VIN (Vehicle Identification Number)</label>
              <input
                type="text"
                id="vin"
                name="vin"
                className="form-input"
                placeholder="17-digit code (must be unique)"
                maxLength="17"
                value={form.vin}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            <div className="form-group span-2">
              <label className="form-label" htmlFor="imageUrl">Image URL</label>
              <input
                type="url"
                id="imageUrl"
                name="imageUrl"
                className="form-input"
                placeholder="https://images.unsplash.com/... (optional)"
                value={form.imageUrl}
                onChange={handleChange}
                disabled={loading}
              />
            </div>

            <div className="form-group span-2">
              <label className="form-label" htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                className="form-input"
                placeholder="Enter detailed description of vehicle characteristics..."
                rows="4"
                style={{ resize: 'none' }}
                value={form.description}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-actions-bar">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              <PlusCircle size={18} /> {loading ? 'Saving...' : 'Add Vehicle'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddVehicle;
