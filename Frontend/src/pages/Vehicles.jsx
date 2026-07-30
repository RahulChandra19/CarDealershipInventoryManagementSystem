import React, { useState, useEffect } from "react";
import api from "../services/api";
import VehicleCard from "../components/VehicleCard";
import { Search, SlidersHorizontal, RefreshCw } from "lucide-react";

const Vehicles = ({ addToast }) => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);

  // Pagination State (for paginated browse)
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 6;

  // Search & Filter state
  const [makeSearch, setMakeSearch] = useState("");
  const [modelSearch, setModelSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  // Available categories list
  const [categories, setCategories] = useState([
    "Electric",
    "Sports",
    "Truck",
    "SUV",
    "Sedan",
  ]);

  // Fetch either paginated list or search list
  const fetchVehicles = async () => {
    setLoading(true);
    try {
      // Check if any search filter is populated
      const hasFilter =
        makeSearch || modelSearch || categoryFilter || minPrice || maxPrice;

      if (hasFilter) {
        setIsSearching(true);
        // Query search endpoint
        const params = {};
        if (makeSearch) params.make = makeSearch;
        if (modelSearch) params.model = modelSearch;
        if (categoryFilter) params.category = categoryFilter;
        if (minPrice) params.minPrice = minPrice;
        if (maxPrice) params.maxPrice = maxPrice;

        const response = await api.get("/vehicles/search", { params });
        setVehicles(response.data);
        setTotalElements(response.data.length);
        setTotalPages(1); // Non-paginated for searches
      } else {
        setIsSearching(false);
        // Query paginated list
        const response = await api.get("/vehicles", {
          params: { page, size: pageSize },
        });
        setVehicles(response.data.content);
        setTotalPages(response.data.totalPages);
        setTotalElements(response.data.totalElements);
      }
    } catch (error) {
      console.error("Failed to fetch vehicles", error);
      addToast("Failed to load vehicles from inventory.", "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
  }, [page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0); // Reset page
    fetchVehicles();
  };

  const handleReset = () => {
    setMakeSearch("");
    setModelSearch("");
    setCategoryFilter("");
    setMinPrice("");
    setMaxPrice("");
    setPage(0);
    setIsSearching(false);
    // Setting page trigger is not enough if page was already 0, so we call fetch directly after state clears
    setTimeout(() => fetchVehicles(), 0);
  };

  return (
    <div className="animate-fade-in">
      <div className="catalog-header">
        <h1 className="catalog-title">Luxury Fleet Inventory</h1>
        <div className="badge badge-customer">
          {totalElements} {totalElements === 1 ? "Vehicle" : "Vehicles"} Total
        </div>
      </div>

      {/* Search & Filter Form */}
      <form
        onSubmit={handleSearchSubmit}
        className="filters-panel glass-panel"
        id="filters-form"
      >
        <div className="filters-grid">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Make</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Tesla"
              value={makeSearch}
              onChange={(e) => setMakeSearch(e.target.value)}
            />
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Model</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Model S"
              value={modelSearch}
              onChange={(e) => setModelSearch(e.target.value)}
            />
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Category</label>
            {/* <select
              className="form-input"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              style={{ color: categoryFilter ? 'white' : '#6b7280' }}
            > */}
            <select
              className="form-input search-category-select"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Price Range ($)</label>
            <div className="price-range-inputs">
              <input
                type="number"
                className="form-input"
                placeholder="Min"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                style={{ width: "100%" }}
              />
              <span className="price-separator">-</span>
              <input
                type="number"
                className="form-input"
                placeholder="Max"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                style={{ width: "100%" }}
              />
            </div>
          </div>

          <div className="filters-actions">
            <button
              type="submit"
              className="btn btn-primary"
              style={{ flex: 1 }}
            >
              <Search size={18} /> Search
            </button>
            <button
              type="button"
              onClick={handleReset}
              className="btn btn-secondary"
            >
              <RefreshCw size={16} />
            </button>
          </div>
        </div>
      </form>

      {/* Inventory Catalog List */}
      {loading ? (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            margin: "4rem 0",
          }}
        >
          <div
            className="glow-effect"
            style={{
              padding: "1rem 2rem",
              borderRadius: "14px",
              background: "rgba(255,255,255,0.03)",
            }}
          >
            Loading luxury fleet...
          </div>
        </div>
      ) : vehicles.length > 0 ? (
        <>
          <div className="vehicles-grid">
            {vehicles.map((v) => (
              <VehicleCard key={v.id} vehicle={v} />
            ))}
          </div>

          {/* Show pagination only when NOT searching (since search endpoint does not return pages) */}
          {!isSearching && totalPages > 1 && (
            <div className="pagination-container">
              <button
                type="button"
                className="btn btn-secondary"
                disabled={page === 0}
                onClick={() => setPage((prev) => prev - 1)}
              >
                Previous
              </button>
              <span className="page-info">
                Page {page + 1} of {totalPages}
              </span>
              <button
                type="button"
                className="btn btn-secondary"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((prev) => prev + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      ) : (
        <div
          className="glass-panel"
          style={{ padding: "4rem", textAlign: "center" }}
        >
          <p style={{ fontSize: "1.2rem", marginBottom: "1.5rem" }}>
            No premium vehicles found matching your criteria.
          </p>
          <button
            type="button"
            onClick={handleReset}
            className="btn btn-primary"
          >
            Reset Filters
          </button>
        </div>
      )}
    </div>
  );
};

export default Vehicles;
