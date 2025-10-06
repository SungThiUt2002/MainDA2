import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAllProducts, searchProducts } from "../../api/productApi";
import { addToCart } from "../../api/cartApi";
import HomePageSearchFilter from "./HomePageSearchFilter";
import "../../styles/Header.css";
import "./HomePage.css";
import API_CONFIG from "../../config/apiConfig";

const IMAGE_BASE_URL = API_CONFIG.PRODUCT_SERVICE.IMAGE_BASE_URL;

const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchResults, setSearchResults] = useState(null);
  const [isSearching, setIsSearching] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const res = await getAllProducts();
        console.log("📦 Products data:", res.data);
        setProducts(res.data || []);
      } catch (err) {
        console.error("Lỗi khi tải sản phẩm:", err);
        setProducts([]);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, []);

  const handleViewProduct = (id) => {
    navigate(`/products/${id}`);
  };

  const handleLogin = () => {
    navigate("/login");
  };

  const handleRegister = () => {
    navigate("/register");
  };

  const handleCart = () => {
    navigate("/cart");
  };

  const handleDashboard = () => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      // Kiểm tra role từ token
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

  const handleAccount = () => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        // Chuyển đến trang profile hoặc account
        navigate("/profile");
      } catch (err) {
        navigate("/login");
      }
    } else {
      navigate("/login");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    alert("Đăng xuất thành công!");
    window.location.reload();
  };

  const handleAddToCart = async (product) => {
    console.log("🛒 handleAddToCart được gọi với product:", product);
    
    const token = localStorage.getItem("accessToken");
    console.log("🔑 Token:", token ? "Có token" : "Không có token");

    if (!token) {
      alert("⚠️ Bạn cần đăng nhập để thêm vào giỏ hàng.");
      return;
    }

    const payload = {
      productId: product.id,
      quantity: 1,
    };

    try {
      const result = await addToCart(payload, token);
      console.log("✅ Thêm vào giỏ hàng thành công:", result);
      alert("✅ Đã thêm sản phẩm vào giỏ hàng!");
    } catch (error) {
      console.error("❌ Lỗi thêm vào giỏ hàng:", error);
      const errorMessage = error.response?.data?.message || error.message || "Lỗi kết nối đến máy chủ.";
      alert("❌ Thêm vào giỏ hàng thất bại: " + errorMessage);
    }
  };

  // Advanced Search Handlers
  const handleAdvancedSearch = async (searchParams) => {
    setIsSearching(true);
    try {
      const response = await searchProducts(searchParams);
      setSearchResults(response.data);
    } catch (error) {
      console.error("Lỗi khi tìm kiếm:", error);
      alert("Có lỗi xảy ra khi tìm kiếm sản phẩm");
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearchReset = () => {
    setSearchResults(null);
  };

  const getCurrentProducts = () => {
    if (searchResults) {
      return searchResults.content || searchResults;
    }
    return products;
  };

  const featuredProducts = getCurrentProducts().slice(0, 8); // Lấy 8 sản phẩm đầu tiên

  // Kiểm tra trạng thái đăng nhập
  const isLoggedIn = !!localStorage.getItem("accessToken");
  let userInfo = null;
  
  if (isLoggedIn) {
    try {
      const token = localStorage.getItem("accessToken");
      const payload = JSON.parse(atob(token.split(".")[1]));
      userInfo = {
        username: payload.sub || payload.username || "User",
        role: payload.role || "USER"
      };
    } catch (err) {
      console.error("Lỗi parse token:", err);
    }
  }

  return (
    <div className="homepage">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="logo">
            <h1>🛍️ TechShop</h1>
          </div>
          
          <nav className="nav">
            <a href="#home" className="nav-link">Trang chủ</a>
            <a href="#search" className="nav-link">Tìm kiếm</a>
            <a href="#products" className="nav-link">Sản phẩm</a>
            <a href="#about" className="nav-link">Giới thiệu</a>
            <a href="#contact" className="nav-link">Liên hệ</a>
          </nav>

          <div className="header-actions">
            <button className="btn btn-cart" onClick={handleCart}>
              🛒 Giỏ hàng
            </button>
            
            {isLoggedIn ? (
              <div className="user-account">
                <button className="btn btn-account" onClick={handleAccount}>
                  👤 {userInfo?.username || "Tài khoản"}
                </button>
                <button className="btn btn-logout" onClick={handleLogout}>
                  🚪 Đăng xuất
                </button>
              </div>
            ) : (
              <>
                <button className="btn btn-login" onClick={handleLogin}>
                  Đăng nhập
                </button>
                <button className="btn btn-register" onClick={handleRegister}>
                  Đăng ký
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Fixed Search Section - Ngay bên dưới header */}
      <section className="fixed-search-section">
        <div className="container">
          {searchResults && (
            <div className="search-results-info">
              <span className="results-count">
                Tìm thấy {searchResults.totalElements || searchResults.length} sản phẩm
              </span>
              <button className="btn btn-clear-search" onClick={handleSearchReset}>
                Xóa kết quả tìm kiếm
              </button>
            </div>
          )}
          
          <HomePageSearchFilter
            onSearch={handleAdvancedSearch}
            onReset={handleSearchReset}
            isSearching={isSearching}
          />
        </div>
      </section>

      {/* Hero Section */}
      <section className="hero" id="home">
        <div className="hero-container">
          <div className="hero-content">
            <h1 className="hero-title">
              Chào mừng đến với <span className="highlight">TechShop</span>
            </h1>
            <p className="hero-subtitle">
              Khám phá bộ sưu tập công nghệ đa dạng với chất lượng cao và giá cả hợp lý
            </p>
            <div className="hero-buttons">
              <button className="btn btn-primary" onClick={() => document.getElementById('products').scrollIntoView({ behavior: 'smooth' })}>
                Xem sản phẩm
              </button>
              <button className="btn btn-secondary" onClick={handleRegister}>
                Đăng ký miễn phí
              </button>
            </div>
          </div>
          <div className="hero-image">
            <div className="hero-placeholder">
              🛍️
            </div>
          </div>
        </div>
      </section>

      {/* Featured Products Section */}
      <section className="featured-products" id="products">
        <div className="container">
          <h2 className="section-title">
            {searchResults ? "Kết quả tìm kiếm" : "Sản phẩm nổi bật"}
          </h2>
          
          {loading ? (
            <div className="loading">
              <div className="spinner"></div>
              <p>Đang tải sản phẩm...</p>
            </div>
          ) : (
            <div className="products-grid">
              {getCurrentProducts() && getCurrentProducts().length > 0 ? getCurrentProducts().map((product) => (
                <div key={product.id} className="product-card">
                  <div className="product-image">
                    {product.images && product.images.length > 0 && product.images[0] ? (
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
                    <button 
                      className="btn btn-cart"
                      onClick={() => {
                        console.log("🔘 Button được click với product:", product);
                        handleAddToCart(product);
                      }}
                    >
                      Thêm vào giỏ hàng
                    </button>
                  </div>
                </div>
              )) : (
                <div className="no-products">
                  <p>Không có sản phẩm nào</p>
                </div>
              )}
            </div>
          )}
          
          {!loading && !searchResults && getCurrentProducts().length > 8 && (
            <div className="view-more">
              <button className="btn btn-primary" onClick={() => document.getElementById('search').scrollIntoView({ behavior: 'smooth' })}>
                Tìm kiếm tất cả sản phẩm
              </button>
            </div>
          )}
          
          {!loading && searchResults && getCurrentProducts().length > 0 && (
            <div className="view-more">
              <button className="btn btn-secondary" onClick={handleSearchReset}>
                Quay lại sản phẩm nổi bật
              </button>
            </div>
          )}
        </div>
      </section>

      {/* About Section */}
      <section className="about" id="about">
        <div className="container">
          <div className="about-content">
            <div className="about-text">
              <h2 className="section-title">Về TechShop</h2>
              <p>
                TechShop là cửa hàng công nghệ hàng đầu, chuyên cung cấp các sản phẩm 
                công nghệ chất lượng cao với giá cả hợp lý. Chúng tôi cam kết mang đến 
                trải nghiệm mua sắm tốt nhất cho khách hàng.
              </p>
              <div className="features">
                <div className="feature">
                  <span className="feature-icon">🚚</span>
                  <div>
                    <h4>Giao hàng nhanh</h4>
                    <p>Giao hàng trong 24h</p>
                  </div>
                </div>
                <div className="feature">
                  <span className="feature-icon">🛡️</span>
                  <div>
                    <h4>Bảo hành chính hãng</h4>
                    <p>Bảo hành 12-24 tháng</p>
                  </div>
                </div>
                <div className="feature">
                  <span className="feature-icon">💰</span>
                  <div>
                    <h4>Giá tốt nhất</h4>
                    <p>Cam kết giá cạnh tranh</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="about-image">
              <div className="about-placeholder">🏪</div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer" id="contact">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h3>TechShop</h3>
              <p>Nơi mua sắm công nghệ tin cậy</p>
              <div className="social-links">
                <a href="#" className="social-link">📘</a>
                <a href="#" className="social-link">📷</a>
                <a href="#" className="social-link">🐦</a>
                <a href="#" className="social-link">📺</a>
              </div>
            </div>
            
            <div className="footer-section">
              <h4>Liên kết</h4>
              <ul>
                <li><a href="#home">Trang chủ</a></li>
                <li><a href="#products">Sản phẩm</a></li>
                <li><a href="#about">Giới thiệu</a></li>
                <li><a href="#contact">Liên hệ</a></li>
              </ul>
            </div>
            
            <div className="footer-section">
              <h4>Hỗ trợ</h4>
              <ul>
                <li><a href="#">Hướng dẫn mua hàng</a></li>
                <li><a href="#">Chính sách bảo hành</a></li>
                <li><a href="#">Chính sách đổi trả</a></li>
                <li><a href="#">FAQ</a></li>
              </ul>
            </div>
            
            <div className="footer-section">
              <h4>Liên hệ</h4>
              <p>📧 info@techshop.com</p>
              <p>📞 1900-1234</p>
              <p>📍 123 Đường ABC, Quận 1, TP.HCM</p>
            </div>
          </div>
          
          <div className="footer-bottom">
            <p>&copy; 2024 TechShop. Tất cả quyền được bảo lưu.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default HomePage; 