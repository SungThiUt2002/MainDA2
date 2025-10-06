import React, { useState, useEffect } from "react";
import { getAllBrands, createBrand, updateBrand, deleteBrand } from "../../api/productApi";
import "./BrandManager.css";

const BrandManager = () => {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editingBrand, setEditingBrand] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    status: "ACTIVE"
  });

  useEffect(() => {
    fetchBrands();
  }, []);

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      const response = await getAllBrands();
      const brandsArray = Array.isArray(response) ? response : [];
      setBrands(brandsArray);
      setError("");
    } catch (err) {
      setError("Không thể tải danh sách thương hiệu");
      console.error("Error fetching brands:", err);
    }
    setLoading(false);
  };

  const handleOpenModal = (brand = null) => {
    console.log("Opening modal with brand:", brand);
    if (brand) {
      setEditingBrand(brand);
      setFormData({
        name: brand.name || "",
        description: brand.description || "",
        status: brand.status || "ACTIVE"
      });
    } else {
      setEditingBrand(null);
      setFormData({
        name: "",
        description: "",
        status: "ACTIVE"
      });
    }
    setShowModal(true);
    console.log("Modal state set to true");
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingBrand(null);
    setFormData({
      name: "",
      description: "",
      status: "ACTIVE"
    });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };



  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      if (editingBrand) {
        await updateBrand(editingBrand.id, formData, token);
      } else {
        await createBrand(formData, token);
      }
      handleCloseModal();
      fetchBrands();
    } catch (err) {
      setError("Lưu thất bại! Vui lòng kiểm tra lại thông tin.");
      console.error("Error saving brand:", err);
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc muốn xóa thương hiệu này?")) return;
    
    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      await deleteBrand(id, token);
      fetchBrands();
    } catch (err) {
      setError("Xóa thất bại!");
      console.error("Error deleting brand:", err);
    }
    setLoading(false);
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      ACTIVE: { text: "Hoạt động", class: "status-active" },
      INACTIVE: { text: "Không hoạt động", class: "status-inactive" }
    };
    const config = statusConfig[status] || statusConfig.ACTIVE;
    return <span className={`status-badge ${config.class}`}>{config.text}</span>;
  };



  if (loading && brands.length === 0) {
    return <div className="loading">Đang tải...</div>;
  }

  console.log("Rendering BrandManager, showModal:", showModal);
  
  return (
    <div className="brand-manager">
      <div className="brand-header">
        <h2>🏷️ Quản lý thương hiệu</h2>
        <button className="add-brand-btn" onClick={() => {
          console.log("Add brand button clicked");
          handleOpenModal();
        }}>
          + Thêm thương hiệu
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="brand-stats">
        <div className="stat-card">
          <div className="stat-icon">🏷️</div>
          <div className="stat-info">
            <h3>Tổng thương hiệu</h3>
            <p className="stat-number">{brands.length}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <h3>Đang hoạt động</h3>
            <p className="stat-number">
              {brands.filter(b => b.status === "ACTIVE").length}
            </p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">⏸️</div>
          <div className="stat-info">
            <h3>Tạm ngưng</h3>
            <p className="stat-number">
              {brands.filter(b => b.status === "INACTIVE").length}
            </p>
          </div>
        </div>
      </div>

      <div className="brand-table-container">
        <table className="brand-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên thương hiệu</th>
              <th>Mô tả</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {brands.map((brand) => (
              <tr key={brand.id}>
                <td>{brand.id}</td>
                                 <td>
                   <div className="brand-info">
                     <span className="brand-name">{brand.name}</span>
                   </div>
                 </td>
                <td>
                  <div className="brand-description">
                    {brand.description ? (
                      brand.description.length > 50 
                        ? `${brand.description.substring(0, 50)}...` 
                        : brand.description
                    ) : (
                      <span className="no-description">Chưa có mô tả</span>
                    )}
                  </div>
                </td>
                <td>{getStatusBadge(brand.status)}</td>
                <td>
                  <div className="action-buttons">
                    <button 
                      className="edit-btn"
                      onClick={() => handleOpenModal(brand)}
                    >
                      Sửa
                    </button>
                    <button 
                      className="delete-btn"
                      onClick={() => handleDelete(brand.id)}
                    >
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={(e) => {
          console.log("Modal overlay clicked");
          if (e.target === e.currentTarget) {
            handleCloseModal();
          }
        }}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingBrand ? "Sửa thương hiệu" : "Thêm thương hiệu mới"}</h3>
              <button className="close-btn" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit} className="brand-form">
              <div className="form-group">
                <label>Tên thương hiệu *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Nhập tên thương hiệu"
                  required
                />
              </div>

              <div className="form-group">
                <label>Mô tả</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  placeholder="Nhập mô tả thương hiệu"
                  rows="3"
                />
              </div>

              <div className="form-group">
                <label>Trạng thái</label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleInputChange}
                >
                  <option value="ACTIVE">Hoạt động</option>
                  <option value="INACTIVE">Không hoạt động</option>
                </select>
              </div>

              <div className="modal-actions">
                <button type="submit" className="save-btn" disabled={loading}>
                  {loading ? "Đang lưu..." : "Lưu"}
                </button>
                <button type="button" onClick={handleCloseModal} className="cancel-btn">
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default BrandManager; 