import React from 'react';
import { Link } from 'react-router-dom';
import { Shield, Eye } from 'lucide-react';

const VehicleCard = ({ vehicle }) => {
  const { id, make, model, year, category, price, quantity, imageUrl } = vehicle;

  // Fallback beautiful image if none is provided
  const displayImage = imageUrl || 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&q=80&w=800';

  const formatPrice = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0
    }).format(value);
  };

  return (
    <article className="vehicle-card glass-panel animate-fade-in" id={`vehicle-card-${id}`}>
      <div className="card-image-container">
        <img 
          src={displayImage} 
          alt={`${year} ${make} ${model}`} 
          className="card-image"
          loading="lazy"
        />
        <span className="card-tag">{category}</span>
      </div>

      <div className="card-body">
        <div className="card-title-row">
          <h3 className="card-title">{make} {model}</h3>
          <span className="card-year">{year}</span>
        </div>

        <p className="card-price">{formatPrice(price)}</p>

        <p className="card-desc">
          {vehicle.description || `Experience the ultimate drive in this premium ${year} ${make} ${model} ${category}.`}
        </p>

        <div className="card-stats">
          <span className="card-stock">
            Stock:{' '}
            {quantity > 0 ? (
              <strong className="stock-in">{quantity} available</strong>
            ) : (
              <strong className="stock-out">Out of Stock</strong>
            )}
          </span>

          <Link to={`/vehicles/${id}`} className="btn btn-secondary" style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}>
            <Eye size={14} /> Details
          </Link>
        </div>
      </div>
    </article>
  );
};

export default VehicleCard;
