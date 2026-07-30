import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Modal from '../components/Modal';
import { ShoppingBag, Edit3, Trash2, PlusCircle, ArrowLeft, ClipboardList, Check } from 'lucide-react';

const VehicleDetails = ({ addToast }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role, user } = useAuth();
  
  const [vehicle, setVehicle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [transactions, setTransactions] = useState([]);
  
  // Inline edit state (Admin)
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({
    make: '', model: '', year: '', category: '', price: '', quantity: '', vin: '', description: '', imageUrl: ''
  });

  // Modals state
  const [purchaseModalOpen, setPurchaseModalOpen] = useState(false);
  const [purchaseQuantity, setPurchaseQuantity] = useState(1);
  
  const [restockModalOpen, setRestockModalOpen] = useState(false);
  const [restockQuantity, setRestockQuantity] = useState(5);
  
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  const fetchVehicleDetails = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/vehicles/${id}`);
      setVehicle(response.data);
      // Initialize edit form values
      setEditForm({
        make: response.data.make,
        model: response.data.model,
        year: response.data.year,
        category: response.data.category,
        price: response.data.price,
        quantity: response.data.quantity,
        vin: response.data.vin || '',
        description: response.data.description || '',
        imageUrl: response.data.imageUrl || ''
      });
      
      // If admin, fetch transaction history
      if (role === 'ADMIN') {
        fetchTransactions();
      }
    } catch (error) {
      console.error('Failed to load vehicle details', error);
      addToast('Vehicle not found or connection lost.', 'error');
      navigate('/vehicles');
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async () => {
    try {
      const response = await api.get(`/vehicles/${id}/transactions`);
      // Sort transactions descending by date
      const sorted = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setTransactions(sorted);
    } catch (error) {
      console.error('Failed to load transactions', error);
    }
  };

  useEffect(() => {
    fetchVehicleDetails();
  }, [id, role]);

  const handlePurchase = async () => {
    if (purchaseQuantity < 1 || purchaseQuantity > vehicle.quantity) {
      addToast('Invalid purchase quantity.', 'error');
      return;
    }

    try {
      await api.post(`/vehicles/${id}/purchase`, { quantity: purchaseQuantity });
      addToast(`Purchased ${purchaseQuantity} unit(s) successfully!`, 'success');
      setPurchaseModalOpen(false);
      // Refresh page data
      fetchVehicleDetails();
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to complete purchase.';
      addToast(msg, 'error');
    }
  };

  const handleRestock = async () => {
    if (restockQuantity < 1) {
      addToast('Restock quantity must be at least 1.', 'error');
      return;
    }

    try {
      await api.post(`/vehicles/${id}/restock`, { quantity: restockQuantity });
      addToast(`Restocked ${restockQuantity} unit(s) successfully!`, 'success');
      setRestockModalOpen(false);
      fetchVehicleDetails();
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to restock vehicle.';
      addToast(msg, 'error');
    }
  };

  const handleDelete = async () => {
    try {
      await api.delete(`/vehicles/${id}`);
      addToast('Vehicle deleted successfully from catalog.', 'success');
      setDeleteModalOpen(false);
      navigate('/vehicles');
    } catch (error) {
      addToast('Failed to delete vehicle.', 'error');
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      // Use PUT for updating all fields
      const response = await api.put(`/vehicles/${id}`, editForm);
      setVehicle(response.data);
      setEditMode(false);
      addToast('Vehicle specs updated successfully!', 'success');
      fetchVehicleDetails();
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to update vehicle specifications.';
      addToast(msg, 'error');
    }
  };

  const formatPrice = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value || 0);
  };

  if (loading || !vehicle) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem 0' }}>
        <div className="glow-effect" style={{ padding: '1rem 2rem', borderRadius: '14px', background: 'rgba(255,255,255,0.03)' }}>
          Retrieving specification specs...
        </div>
      </div>
    );
  }

  const displayImage = vehicle.imageUrl || 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&q=80&w=800';

  return (
    <div className="animate-fade-in">
      <button onClick={() => navigate('/vehicles')} className="btn btn-secondary" style={{ marginBottom: '1.5rem' }}>
        <ArrowLeft size={16} /> Back to Catalog
      </button>

      <div className="details-grid">
        {/* Left Side: Vehicle Image & Transaction Log if Admin */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div className="details-image-panel glass-panel">
            <img src={displayImage} alt={`${vehicle.make} ${vehicle.model}`} className="details-img" />
          </div>

          {role === 'ADMIN' && (
            <div className="glass-panel" style={{ padding: '2rem' }}>
              <h3 className="details-desc-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
                <ClipboardList size={20} /> Inventory Transaction Log
              </h3>
              
              {transactions.length > 0 ? (
                <div className="transaction-list">
                  {transactions.map((t) => (
                    <div key={t.id} className="transaction-item">
                      <div>
                        <span className={`badge ${t.type === 'RESTOCK' ? 'badge-success' : 'badge-customer'}`} style={{ marginRight: '0.5rem' }}>
                          {t.type}
                        </span>
                        <strong>{t.quantityChange > 0 ? `+${t.quantityChange}` : t.quantityChange} units</strong>
                      </div>
                      <div style={{ textAlign: 'right', fontSize: '0.8rem', color: '#9ca3af' }}>
                        <div>Rate: {formatPrice(t.priceAtTime)}</div>
                        <div>{new Date(t.createdAt).toLocaleString()}</div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ color: '#9ca3af', fontStyle: 'italic', fontSize: '0.9rem' }}>No inventory transactions recorded.</p>
              )}
            </div>
          )}
        </div>

        {/* Right Side: Vehicle Specifications & Actions */}
        <div>
          {editMode ? (
            /* Admin Edit Specs Form */
            <form onSubmit={handleEditSubmit} className="glass-panel details-info-panel" id="edit-vehicle-form">
              <h2 className="details-title" style={{ marginBottom: '1.5rem', fontSize: '1.8rem' }}>Edit Specifications</h2>
              
              <div className="form-grid" style={{ gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Make</label>
                  <input
                    type="text" required className="form-input"
                    value={editForm.make} onChange={(e) => setEditForm({...editForm, make: e.target.value})}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Model</label>
                  <input
                    type="text" required className="form-input"
                    value={editForm.model} onChange={(e) => setEditForm({...editForm, model: e.target.value})}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Year</label>
                  <input
                    type="number" required min="1900" className="form-input"
                    value={editForm.year} onChange={(e) => setEditForm({...editForm, year: parseInt(e.target.value)})}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Category</label>
                  <select
                    required className="form-input"
                    value={editForm.category} onChange={(e) => setEditForm({...editForm, category: e.target.value})}
                  >
                    <option value="Electric">Electric</option>
                    <option value="Sports">Sports</option>
                    <option value="Truck">Truck</option>
                    <option value="SUV">SUV</option>
                    <option value="Sedan">Sedan</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Price ($)</label>
                  <input
                    type="number" required step="0.01" className="form-input"
                    value={editForm.price} onChange={(e) => setEditForm({...editForm, price: parseFloat(e.target.value)})}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">VIN</label>
                  <input
                    type="text" className="form-input"
                    value={editForm.vin} onChange={(e) => setEditForm({...editForm, vin: e.target.value})}
                  />
                </div>
                <div className="form-group span-2">
                  <label className="form-label">Image URL</label>
                  <input
                    type="url" className="form-input"
                    value={editForm.imageUrl} onChange={(e) => setEditForm({...editForm, imageUrl: e.target.value})}
                  />
                </div>
                <div className="form-group span-2">
                  <label className="form-label">Description</label>
                  <textarea
                    rows="3" className="form-input" style={{ resize: 'none' }}
                    value={editForm.description} onChange={(e) => setEditForm({...editForm, description: e.target.value})}
                  />
                </div>
              </div>

              <div className="form-actions-bar" style={{ marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setEditMode(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  <Check size={18} /> Save Changes
                </button>
              </div>
            </form>
          ) : (
            /* Vehicle Specs View */
            <div className="glass-panel details-info-panel" style={{ flex: 1 }}>
              <div className="details-header">
                <span className="badge badge-customer" style={{ marginBottom: '0.75rem' }}>
                  {vehicle.category}
                </span>
                <div className="details-title-row">
                  <h2 className="details-title">{vehicle.make} {vehicle.model}</h2>
                  <span className="badge badge-admin" style={{ fontSize: '1rem', padding: '0.4rem 1rem' }}>{vehicle.year}</span>
                </div>
                <p className="details-price">{formatPrice(vehicle.price)}</p>
              </div>

              <div className="details-metadata" style={{ marginBottom: '1.5rem' }}>
                <span>Stock Quantity:</span>
                {vehicle.quantity > 0 ? (
                  <span className="badge badge-success">{vehicle.quantity} Available</span>
                ) : (
                  <span className="badge badge-error">Out of Stock</span>
                )}
                {vehicle.vin && (
                  <>
                    <span style={{ color: '#6b7280' }}>|</span>
                    <span>VIN:</span>
                    <span className="details-vin">{vehicle.vin}</span>
                  </>
                )}
              </div>

              <h3 className="details-desc-title">Description</h3>
              <p className="details-desc">
                {vehicle.description || `This custom ${vehicle.year} ${vehicle.make} ${vehicle.model} brings high performance and executive utility to its class. Fitted with premium equipment, it stands out with standard drive assurance and elite specifications.`}
              </p>

              <div className="details-actions-section">
                {/* Customer Purchase Button */}
                {role !== 'ADMIN' && (
                  <button
                    onClick={() => {
                      setPurchaseQuantity(1);
                      setPurchaseModalOpen(true);
                    }}
                    className="btn btn-primary"
                    disabled={vehicle.quantity <= 0}
                    style={{ width: '100%', padding: '1.1rem' }}
                  >
                    <ShoppingBag size={20} /> {vehicle.quantity > 0 ? 'Purchase Vehicle' : 'Temporarily Out of Stock'}
                  </button>
                )}

                {/* Admin Management Panel */}
                {role === 'ADMIN' && (
                  <div className="admin-action-bar">
                    <button onClick={() => setEditMode(true)} className="btn btn-secondary" style={{ flex: 1 }}>
                      <Edit3 size={18} /> Edit Specs
                    </button>
                    <button onClick={() => setRestockModalOpen(true)} className="btn btn-secondary" style={{ flex: 1 }}>
                      <PlusCircle size={18} /> Restock
                    </button>
                    <button onClick={() => setDeleteModalOpen(true)} className="btn btn-danger" style={{ flex: 1 }}>
                      <Trash2 size={18} /> Delete
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Confirmation Modals */}

      {/* Purchase Modal */}
      <Modal
        isOpen={purchaseModalOpen}
        onClose={() => setPurchaseModalOpen(false)}
        title="Confirm Purchase"
        footer={
          <>
            <button onClick={() => setPurchaseModalOpen(false)} className="btn btn-secondary">Cancel</button>
            <button onClick={handlePurchase} className="btn btn-primary">Confirm Purchase</button>
          </>
        }
      >
        <p style={{ marginBottom: '1.25rem' }}>You are purchasing <strong>{vehicle.make} {vehicle.model}</strong>.</p>
        <div className="form-group">
          <label className="form-label" htmlFor="buy-qty">Select Quantity</label>
          <input
            type="number"
            id="buy-qty"
            className="form-input"
            min="1"
            max={vehicle.quantity}
            value={purchaseQuantity}
            onChange={(e) => setPurchaseQuantity(Math.min(vehicle.quantity, Math.max(1, parseInt(e.target.value) || 1)))}
          />
        </div>
        <div style={{ borderTop: '1px solid var(--glass-border)', paddingTop: '1rem', marginTop: '1rem', display: 'flex', justifyContent: 'space-between', fontWeight: '600' }}>
          <span>Total Price:</span>
          <span style={{ color: '#60a5fa', fontSize: '1.15rem' }}>{formatPrice(vehicle.price * purchaseQuantity)}</span>
        </div>
      </Modal>

      {/* Restock Modal (Admin Only) */}
      <Modal
        isOpen={restockModalOpen}
        onClose={() => setRestockModalOpen(false)}
        title="Restock Vehicle Inventory"
        footer={
          <>
            <button onClick={() => setRestockModalOpen(false)} className="btn btn-secondary">Cancel</button>
            <button onClick={handleRestock} className="btn btn-primary">Add to Stock</button>
          </>
        }
      >
        <p style={{ marginBottom: '1.25rem' }}>Add stock units for <strong>{vehicle.make} {vehicle.model}</strong>.</p>
        <div className="form-group">
          <label className="form-label" htmlFor="restock-qty">Restock Quantity</label>
          <input
            type="number"
            id="restock-qty"
            className="form-input"
            min="1"
            value={restockQuantity}
            onChange={(e) => setRestockQuantity(Math.max(1, parseInt(e.target.value) || 1))}
          />
        </div>
      </Modal>

      {/* Delete Confirmation Modal (Admin Only) */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="Delete Vehicle"
        footer={
          <>
            <button onClick={() => setDeleteModalOpen(false)} className="btn btn-secondary">Cancel</button>
            <button onClick={handleDelete} className="btn btn-danger">Confirm Delete</button>
          </>
        }
      >
        <p>Are you sure you want to soft delete the <strong>{vehicle.make} {vehicle.model}</strong>?</p>
        <p style={{ color: '#fb7185', marginTop: '0.5rem', fontSize: '0.88rem' }}>
          This will flag the vehicle as inactive. It will be hidden from the active catalog listing.
        </p>
      </Modal>
    </div>
  );
};

export default VehicleDetails;
