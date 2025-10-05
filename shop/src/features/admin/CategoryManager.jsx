import React, { useState, useEffect } from "react";
import { getAllCategories, createCategory, updateCategory, deleteCategory } from "../../api/productApi";
import "./CategoryManager.css";

const CategoryManager = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    status: "ACTIVE"
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await getAllCategories();
      const categoriesArray = Array.isArray(response) ? response : [];
      
      // Gán default status cho các category từ backend (vì backend không có field status)
      const categoriesWithStatus = categoriesArray.map(category => ({
        ...category,
        status: category.status || "ACTIVE" // Default status
      }));
      
      setCategories(categoriesWithStatus);
      setError("");
    } catch (err) {
      setError("Không thể tải danh sách danh mục");
      console.error("Error fetching categories:", err);
    }
    setLoading(false);
  };

  const handleOpenModal = (category = null) => {
    if (category) {
      setEditingCategory(category);
      setFormData({
        name: category.name || "",
        description: category.description || "",
        status: "ACTIVE" // Default status vì backend không có field này
      });
    } else {
      setEditingCategory(null);
      setFormData({
        name: "",
        description: "",
        status: "ACTIVE"
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingCategory(null);
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
      
      // Chỉ gửi name và description lên backend, bỏ status
      const backendData = {
        name: formData.name,
        description: formData.description
      };
      
      if (editingCategory) {
        await updateCategory(editingCategory.id, backendData, token);
      } else {
        await createCategory(backendData, token);
      }
      handleCloseModal();
      fetchCategories();
    } catch (err) {
      setError("Lưu thất bại! Vui lòng kiểm tra lại thông tin.");
      console.error("Error saving category:", err);
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc muốn xóa danh mục này?")) return;
    
    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      await deleteCategory(id, token);
      fetchCategories();
    } catch (err) {
      setError("Xóa thất bại!");
      console.error("Error deleting category:", err);
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




  if (loading && categories.length === 0) {
    return <div className="loading">Đang tải...</div>;
  }

  return (
    <div className="category-manager">
      <div className="category-header">
        <h2>📂 Quản lý danh mục</h2>
        <button className="add-category-btn" onClick={() => handleOpenModal()}>
          + Thêm danh mục
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="category-stats">
        <div className="stat-card">
          <div className="stat-icon">📂</div>
          <div className="stat-info">
            <h3>Tổng danh mục</h3>
            <p className="stat-number">{categories.length}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <h3>Đang hoạt động</h3>
            <p className="stat-number">
              {categories.filter(c => c.status === "ACTIVE").length}
            </p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">⏸️</div>
          <div className="stat-info">
            <h3>Tạm ngưng</h3>
            <p className="stat-number">
              {categories.filter(c => c.status === "INACTIVE").length}
            </p>
          </div>
        </div>
      </div>

      <div className="category-grid">
        {categories.length > 0 ? (
          categories.map((category) => (
            <div key={category.id} className="category-card">
                             <div className="category-image">
                 <div className="category-image-placeholder">
                   📂
                 </div>
               </div>
              
              <div className="category-content">
                <div className="category-header-info">
                  <h3 className="category-name">{category.name}</h3>
                  {getStatusBadge(category.status || "ACTIVE")}
                </div>
                
                <p className="category-description">
                  {category.description || "Chưa có mô tả"}
                </p>
                
                <div className="category-meta">
                  <span className="product-count">
                    {category.products?.length || 0} sản phẩm
                  </span>
                </div>
                
                <div className="category-actions">
                  <button 
                    className="edit-btn"
                    onClick={() => handleOpenModal(category)}
                  >
                    ✏️ Sửa
                  </button>
                  <button 
                    className="delete-btn"
                    onClick={() => handleDelete(category.id)}
                  >
                    🗑️ Xóa
                  </button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="no-categories">
            <div className="no-categories-icon">📂</div>
            <h3>Chưa có danh mục nào</h3>
            <p>Bắt đầu bằng cách tạo danh mục đầu tiên</p>
            <button 
              className="add-first-btn"
              onClick={() => handleOpenModal()}
            >
              + Thêm danh mục đầu tiên
            </button>
          </div>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3>{editingCategory ? "Sửa danh mục" : "Thêm danh mục mới"}</h3>
              <button className="close-btn" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit} className="category-form">
              <div className="form-group">
                <label>Tên danh mục *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Nhập tên danh mục"
                  required
                />
              </div>

              <div className="form-group">
                <label>Mô tả</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  placeholder="Nhập mô tả danh mục"
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

export default CategoryManager; 