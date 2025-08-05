import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getProductsByCategory, getAllCategories } from "../../api/productApi";
import "../../styles/Header.css";
import "./CategoryProducts.css";

const IMAGE_BASE_URL = "http://localhost:9001/images/";

const CategoryProducts = () => {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [category, setCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Lấy thông tin category
        const categoriesRes = await getAllCategories();
        const currentCategory = categoriesRes.data.find(cat => cat.id == categoryId);
        setCategory(currentCategory);

        // Lấy sản phẩm theo category
        const productsRes = await getProductsByCategory(categoryId);
        setProducts(productsRes.data);
      } catch (err) {
        console.error("Lỗi khi tải dữ liệu:", err);
        setError("Không thể tải sản phẩm trong danh mục này.");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [categoryId]);

  const handleViewProduct = (id) => {
    navigate(`/products/${id}`);
  };

  const handleBackToHome = () => {
    navigate("/");
  };

  const handleCart = () => {
    navigate("/cart");
  };

  const handleLogin = () => {
    navigate("/login");
  };

  const handleRegister = () => {
    navigate("/register");
  };

  const handleDashboard = () => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        if (payload.role === "ADMIN") {
          navigate("/admin/dashboard");
        } else {
          // User thường sẽ ở lại HomePage
          alert("Bạn đã đăng nhập! Chào mừng quay lại.");
        }
      } catch (err) {
        alert("Bạn đã đăng nhập! Chào mừng quay lại.");
      }
    } else {
      navigate("/login");
    }
  };

  if (loading) {
    return (
      <div className="category-products-page">
        <div className="loading">
          <div className="spinner"></div>
          <p>Đang tải sản phẩm...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="category-products-page">
        <div className="error">
          <p>{error}</p>
          <button className="btn btn-primary" onClick={handleBackToHome}>
            Về trang chủ
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="category-products-page">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="logo" onClick={handleBackToHome}>
            <h1>🛍️ TechShop</h1>
          </div>
          
          <nav className="nav">
            <a href="/" className="nav-link">Trang chủ</a>
            <a href="/#products" className="nav-link">Sản phẩm</a>
            <a href="/#categories" className="nav-link">Danh mục</a>
            <a href="/#about" className="nav-link">Giới thiệu</a>
            <a href="/#contact" className="nav-link">Liên hệ</a>
          </nav>

          <div className="header-actions">
            <button className="btn btn-cart" onClick={handleCart}>
              🛒 Giỏ hàng
            </button>
            <button className="btn btn-login" onClick={handleLogin}>
              Đăng nhập
            </button>
            <button className="btn btn-register" onClick={handleRegister}>
              Đăng ký
            </button>
            <button className="btn btn-dashboard" onClick={handleDashboard}>
              Dashboard
            </button>
          </div>
        </div>
      </header>

      {/* Breadcrumb */}
      <nav className="breadcrumb">
        <span onClick={handleBackToHome} className="breadcrumb-link">Trang chủ</span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">{category?.name || 'Danh mục'}</span>
      </nav>

      {/* Category Info */}
      <div className="category-info">
        <div className="container">
          <h1 className="category-title">{category?.name}</h1>
          {category?.description && (
            <p className="category-description">{category.description}</p>
          )}
          <p className="product-count">
            {products.length} sản phẩm trong danh mục này
          </p>
        </div>
      </div>

      {/* Products Grid */}
      <div className="products-section">
        <div className="container">
          {products.length === 0 ? (
            <div className="no-products">
              <p>Không có sản phẩm nào trong danh mục này.</p>
              <button className="btn btn-primary" onClick={handleBackToHome}>
                Về trang chủ
              </button>
            </div>
          ) : (
            <div className="products-grid">
              {products.map((product) => (
                <div key={product.id} className="product-card">
                  <div className="product-image">
                    {product.images && product.images.length > 0 ? (
                      <img 
                        src={`${IMAGE_BASE_URL}${product.images[0].url}`} 
                        alt={product.name}
                        onError={(e) => {
                          e.target.src = "https://via.placeholder.com/300x300?text=No+Image";
                        }}
                      />
                    ) : (
                      <div className="product-placeholder">📱</div>
                    )}
                  </div>
                  
                  <div className="product-info">
                    <h3 className="product-name">{product.name}</h3>
                    <p className="product-description">
                      {product.description?.substring(0, 100)}...
                    </p>
                    <div className="product-price">
                      <span className="price">
                        {product.price?.toLocaleString('vi-VN')} ₫
                      </span>
                    </div>
                    <button 
                      className="btn btn-view"
                      onClick={() => handleViewProduct(product.id)}
                    >
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CategoryProducts; 