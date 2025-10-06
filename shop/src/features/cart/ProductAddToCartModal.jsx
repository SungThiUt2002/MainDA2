// File: src/features/cart/ProductAddToCartModal.jsx

import React, { useEffect, useState } from "react";
import "./ProductAddToCartModal.css";
import API_CONFIG from "../../config/apiConfig";

const ProductAddToCartModal = ({ product, onClose, onConfirm }) => {
  const [variants, setVariants] = useState([]);
  const [selectedVariantId, setSelectedVariantId] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [stock, setStock] = useState(0);
  const [price, setPrice] = useState(0);
  const [selectedImage, setSelectedImage] = useState("");

  useEffect(() => {
    const fetchVariants = async () => {
      try {
        const res = await fetch(`${API_CONFIG.PRODUCT_SERVICE.BASE_URL}/api/products/${product.id}/variants`);
        const data = await res.json();
        setVariants(data);

        if (data.length > 0) {
          const defaultVariant = data[0];
          setSelectedVariantId(defaultVariant.id);
          setStock(defaultVariant.stock);
          setPrice(defaultVariant.price);
          setSelectedImage(defaultVariant.image || "");
        }
      } catch (err) {
        console.error("Lỗi khi load biến thể:", err);
      }
    };

    if (product) fetchVariants();
  }, [product]);

  const handleVariantChange = (e) => {
    const variantId = parseInt(e.target.value);
    const variant = variants.find((v) => v.id === variantId);
    if (!variant) return;
    setSelectedVariantId(variantId);
    setStock(variant.stock);
    setPrice(variant.price);
    setQuantity(1);
    setSelectedImage(variant.image || "");
  };

  const handleQuantityChange = (e) => {
    let val = parseInt(e.target.value);
    if (isNaN(val)) return;
    if (val > stock) val = stock;
    if (val < 1) val = 1;
    setQuantity(val);
  };

  const handleConfirm = () => {
    if (!selectedVariantId || quantity < 1 || quantity > stock) return;

    const selectedVariant = variants.find(v => v.id === selectedVariantId);
    if (!selectedVariant) return;

    onConfirm(
      {
        ...product,
        defaultVariantId: selectedVariant.id,
        price: selectedVariant.price
      },
      quantity
    );
  };

  if (!product) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        <h3>Thêm vào giỏ hàng: {product.name}</h3>

        {selectedImage && (
          <div className="variant-image">
            <img
              src={`${API_CONFIG.PRODUCT_SERVICE.IMAGE_BASE_URL}${selectedImage}`}
              alt="variant"
            />
          </div>
        )}

        <div className="form-group">
          <label>Chọn biến thể:</label>
          <select value={selectedVariantId || ""} onChange={handleVariantChange}>
            {variants.map((variant) => (
              <option key={variant.id} value={variant.id}>
                {variant.sku} — Màu: {variant.color}, Size: {variant.size}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Số lượng:</label>
          <input
            type="number"
            min="1"
            max={stock}
            value={quantity}
            onChange={handleQuantityChange}
          />
        </div>

        <div className="price-display">
          Giá: {price?.toLocaleString()}₫ — Tồn kho: {stock}
        </div>

        <div className="modal-actions">
          <button onClick={handleConfirm}>Xác nhận</button>
          <button className="cancel" onClick={onClose}>Hủy</button>
        </div>
      </div>
    </div>
  );
};

export default ProductAddToCartModal;
