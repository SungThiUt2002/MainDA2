// File: src/features/customer/ProductList.jsx

import React, { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { getAllProducts } from "../../api/productApi";
import { addToCart } from "../../api/cartApi";
import Slider from "react-slick";
import "./ProductList.css";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import API_CONFIG from "../../config/apiConfig";

const IMAGE_BASE_URL = API_CONFIG.PRODUCT_SERVICE.IMAGE_BASE_URL;

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [currentSlideIndex, setCurrentSlideIndex] = useState({});
  const sliderRefs = useRef({});
  const navigate = useNavigate();

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await getAllProducts();
        setProducts(res.data);
      } catch (err) {
        console.error("Lỗi khi tải sản phẩm:", err);
      }
    };
    fetch();
  }, []);

  const handleBeforeChange = (productId) => (oldIndex, newIndex) => {
    setCurrentSlideIndex((prev) => ({ ...prev, [productId]: newIndex }));
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
      await addToCart(payload, token);
      alert("✅ Đã thêm sản phẩm vào giỏ hàng!");
    } catch (error) {
      console.error("❌ Lỗi thêm vào giỏ hàng:", error);
      const errorMessage = error.response?.data?.message || error.message || "Lỗi kết nối đến máy chủ.";
      alert("❌ Thêm vào giỏ hàng thất bại: " + errorMessage);
    }
  };

  const handleViewDetail = (id) => {
    navigate(`/products/${id}`);
  };

  const sliderSettings = (productId) => ({
    dots: false,
    infinite: true,
    speed: 300,
    slidesToShow: 1,
    slidesToScroll: 1,
    arrows: true,
    nextArrow: <SampleNextArrow />,
    prevArrow: <SamplePrevArrow />,
    beforeChange: handleBeforeChange(productId),
  });

  function SampleNextArrow(props) {
    const { onClick } = props;
    return <div className="arrow next" onClick={onClick} />;
  }

  function SamplePrevArrow(props) {
    const { onClick } = props;
    return <div className="arrow prev" onClick={onClick} />;
  }

  return (
    <div className="product-grid">
      {products.length === 0 ? (
        <p className="no-products">Không có sản phẩm nào.</p>
      ) : (
        products.map((product) => (
          <div className="product-card" key={product.id}>
            {product.images && product.images.length > 0 ? (
              <div className="slider-wrapper">
                <Slider
                  {...sliderSettings(product.id)}
                  className="product-slider"
                  ref={(ref) => (sliderRefs.current[product.id] = ref)}
                >
                  {product.images.map((img, index) => (
                    <div key={img.id} className="slider-image-wrapper">
                      <img
                        src={`${IMAGE_BASE_URL}${img.url}`}
                        alt={`${product.name} ${index + 1}`}
                        className="product-img"
                      />
                    </div>
                  ))}
                </Slider>

                {product.images.length > 1 && (
                  <>
                    <div className="image-counter">
                      {(currentSlideIndex[product.id] || 0) + 1} / {product.images.length}
                    </div>
                    <div className="thumbnail-list">
                      {product.images.map((img, index) => (
                        <img
                          key={img.id}
                          src={`${IMAGE_BASE_URL}${img.url}`}
                          alt="thumb"
                          className={`thumbnail-img ${
                            currentSlideIndex[product.id] === index ? "active" : ""
                          }`}
                          onClick={() => {
                            sliderRefs.current[product.id]?.slickGoTo(index);
                          }}
                        />
                      ))}
                    </div>
                  </>
                )}
              </div>
            ) : (
              <img
                src="/placeholder.jpg"
                alt={product.name}
                className="product-img"
              />
            )}

            <h3 className="product-title">{product.name}</h3>
            <p className="price">{product.price?.toLocaleString()} ₫</p>
            <div className="button-group">
              <button
                className="detail-button"
                onClick={() => handleViewDetail(product.id)}
              >
                Xem chi tiết
              </button>
              <button
                className="cart-button"
                onClick={() => {
                  console.log("🔘 Button được click với product:", product);
                  handleAddToCart(product);
                }}
              >
                Thêm vào giỏ hàng
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default ProductList;
