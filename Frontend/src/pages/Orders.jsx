import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Modal from '../components/Modal';
import { ShoppingBag, RefreshCw, XCircle } from 'lucide-react';

const Orders = ({ addToast }) => {
  const { role } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Cancel Order Modal State
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const endpoint = role === 'ADMIN' ? '/orders' : '/orders/me';
      const response = await api.get(endpoint);
      
      // Sort orders descending by created date
      const sorted = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setOrders(sorted);
    } catch (error) {
      console.error('Failed to load orders', error);
      addToast('Failed to retrieve order history.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [role]);

  const handleCancelConfirm = (order) => {
    setOrderToCancel(order);
    setCancelModalOpen(true);
  };

  const handleCancelOrder = async () => {
    if (!orderToCancel) return;
    try {
      await api.delete(`/orders/${orderToCancel.id}`);
      addToast(`Order #${orderToCancel.id} cancelled successfully!`, 'success');
      setCancelModalOpen(false);
      setOrderToCancel(null);
      fetchOrders(); // Refresh table
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to cancel order.';
      addToast(msg, 'error');
    }
  };

  const formatPrice = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CANCELLED':
        return <span className="badge badge-error">Cancelled</span>;
      case 'PAID':
      case 'COMPLETED':
      case 'CONFIRMED':
        return <span className="badge badge-success">Confirmed</span>;
      default:
        return <span className="badge badge-warning">{status}</span>;
    }
  };

  return (
    <div className="animate-fade-in">
      <div className="catalog-header">
        <h1 className="catalog-title">
          {role === 'ADMIN' ? 'All Customer Orders' : 'My Purchase Orders'}
        </h1>
        <button onClick={fetchOrders} className="btn btn-secondary" style={{ padding: '0.5rem 1rem' }}>
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      <div className="order-board-panel glass-panel">
        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem 0' }}>
            <div className="glow-effect" style={{ padding: '0.8rem 1.5rem', borderRadius: '12px', background: 'rgba(255,255,255,0.03)' }}>
              Retrieving orders ledger...
            </div>
          </div>
        ) : orders.length > 0 ? (
          <div className="table-responsive">
            <table className="dashboard-table" id="orders-dashboard-table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  {role === 'ADMIN' && <th>Client</th>}
                  <th>Vehicle Specifications</th>
                  <th>Quantity</th>
                  <th>Total Cost</th>
                  <th>Status</th>
                  <th>Purchased At</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id} id={`order-row-${o.id}`}>
                    <td style={{ fontWeight: '600' }}>#{o.id}</td>
                    {role === 'ADMIN' && <td>{o.username}</td>}
                    <td>
                      {o.vehicleMake} {o.vehicleModel}
                    </td>
                    <td>{o.quantity}</td>
                    <td style={{ color: '#60a5fa', fontWeight: '600' }}>{formatPrice(o.totalPrice)}</td>
                    <td>{getStatusBadge(o.status)}</td>
                    <td style={{ fontSize: '0.85rem', color: '#9ca3af' }}>
                      {new Date(o.createdAt).toLocaleString()}
                    </td>
                    <td>
                      {o.status !== 'CANCELLED' ? (
                        <button
                          onClick={() => handleCancelConfirm(o)}
                          className="btn btn-danger"
                          style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                        >
                          <XCircle size={14} /> Cancel
                        </button>
                      ) : (
                        <span style={{ color: '#6b7280', fontSize: '0.85rem', fontStyle: 'italic' }}>Inactive</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ padding: '3rem 0', textAlign: 'center' }}>
            <ShoppingBag size={48} style={{ color: '#4b5563', marginBottom: '1rem' }} />
            <p style={{ color: '#9ca3af' }}>No order history recorded.</p>
          </div>
        )}
      </div>

      {/* Cancel Order Confirmation Modal */}
      <Modal
        isOpen={cancelModalOpen}
        onClose={() => setCancelModalOpen(false)}
        title="Cancel Purchase Order"
        footer={
          <>
            <button onClick={() => setCancelModalOpen(false)} className="btn btn-secondary">Keep Order</button>
            <button onClick={handleCancelOrder} className="btn btn-danger">Cancel Order</button>
          </>
        }
      >
        {orderToCancel && (
          <p>
            Are you sure you want to cancel Order <strong>#{orderToCancel.id}</strong> for{' '}
            <strong>{orderToCancel.quantity} {orderToCancel.vehicleMake} {orderToCancel.vehicleModel}</strong>?
          </p>
        )}
        <p style={{ color: '#fb7185', marginTop: '0.5rem', fontSize: '0.88rem' }}>
          This will release the quantities back into the available inventory stock.
        </p>
      </Modal>
    </div>
  );
};

export default Orders;
