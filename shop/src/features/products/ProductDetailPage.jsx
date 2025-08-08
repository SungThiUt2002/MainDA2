import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getProductById } from "../../api/productApi";
import { getAvailableQuantity, getSoldQuantity } from "../../api/inventoryApi";
import { addToCart } from "../../api/cartApi";
import "./ProductDetailPage.css";

const ProductDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [brandName, setBrandName] = useState("");
  const [mainImage, setMainImage] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stockData, setStockData] = useState({
    availableQuantity: 0,
    soldQuantity: 0
  });

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        console.log("Fetching product with ID:", id); // Debug log
        const res = await getProductById(id);
        console.log("Product data:", res.data); // Debug log

        setProduct(res.data);
        setMainImage(res.data.images?.[0] ? (typeof res.data.images[0] === 'object' ? res.data.images[0].url : res.data.images[0]) : "");

        // Lấy thông tin thương hiệu nếu có brandId
        if (res.data.brand?.id) {
          try {
            const brandRes = await fetch(`http://localhost:9001/api/v1/brands/${res.data.brand.id}`);
            if (brandRes.ok) {
              const brandData = await brandRes.json();
              setBrandName(brandData.name || "Không xác định");
            }
          } catch (brandErr) {
            console.error("Lỗi khi tải thông tin brand:", brandErr);
            setBrandName(res.data.brand?.name || "Không xác định");
          }
        } else if (res.data.brand?.name) {
          setBrandName(res.data.brand.name);
        } else {
          setBrandName("Không có thương hiệu");
        }


        try {
          const [availableRes, soldRes] = await Promise.all([
            getAvailableQuantity(res.data.id),
            getSoldQuantity(res.data.id)
          ]);
          setStockData({
            availableQuantity: availableRes.data || 0,
            soldQuantity: soldRes.data || 0
          });

        } catch (invErr) {
          console.error("Lỗi khi lấy thông tin tồn kho:", invErr);
        }

      } catch (err) {
        console.error("Error fetching product:", err); // Debug log
        setError("Không thể tải chi tiết sản phẩm.");
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchProduct();
    }
  }, [id]);

  const handleAddToCart = async () => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      alert("⚠️ Bạn cần đăng nhập để thêm vào giỏ hàng.");
      return;
    }

    const payload = {
      productId: product.id,
      quantity: 1,
    };

    try {
      await addToCart(payload, token);
      alert("✅ Đã thêm sản phẩm vào giỏ hàng!");
    } catch (error) {
      console.error("❌ Lỗi thêm vào giỏ hàng:", error);
      const errorMessage = error.response?.data?.message || error.message || "Lỗi kết nối đến máy chủ.";
      alert("❌ Thêm vào giỏ hàng thất bại: " + errorMessage);
    }
  };

  if (loading) return <div className="loading">Đang tải...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!product) return null;

  return (
    <div className="product-detail-wrapper">
      <header className="top-navbar">
        <div className="navbar-left">
          <div className="logo" onClick={() => navigate("/")}>🛍️ MyShop</div>
        </div>
        <div className="navbar-center">
          <input type="text" className="search-input" placeholder="Công Chúa Tung Deal" />
          <button className="search-btn">🔍</button>
        </div>
        <div className="navbar-right">
          <div className="cart-icon" onClick={() => navigate("/cart")}>🛒</div>
        </div>
      </header>

      <nav className="breadcrumb">Sản phẩm / {product.name}</nav>

      <div className="product-body">
        <div className="image-gallery">
          <div className="main-image">
            <img
              src={`http://localhost:9001/images/${mainImage}`}
              alt={product.name}
              className="product-main-img"
            />
          </div>
          <div className="thumbnail-row">
            <div className="thumbnail-list-horizontal">
              {product.images && product.images.length > 0 ? product.images.map((img, index) => (
                <img
                  key={img.id || img || index}
                  src={`http://localhost:9001/images/${typeof img === 'object' ? img.url : img}`}
                  alt="thumbnail"
                  className={`thumbnail ${mainImage === (typeof img === 'object' ? img.url : img) ? "active" : ""}`}
                  onClick={() => setMainImage(typeof img === 'object' ? img.url : img)}
                />
              )) : null}
            </div>
          </div>
        </div>

        <div className="info-section">
          <h1 className="product-title">{product.name}</h1>
          <p className="product-price">{product.price?.toLocaleString() || "0"} ₫</p>
          <p><strong>Đã bán:</strong> {stockData.soldQuantity}</p>
          <p><strong>Tồn kho:</strong> {stockData.availableQuantity}</p>
          <p><strong>Thương hiệu:</strong> {brandName}</p>
          <div className="product-description">
            <strong>Mô tả:</strong>
            <p className="product-desc-text">{product.description || "Chưa có mô tả"}</p>
          </div>
          <button className="add-to-cart" onClick={handleAddToCart}>
            Thêm vào giỏ hàng
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;