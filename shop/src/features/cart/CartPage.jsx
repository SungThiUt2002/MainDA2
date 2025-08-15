// 📁 src/pages/CartPage.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchCartItems,
  fetchProductImage,
  decreaseCartItemQuantity,
  increaseCartItemQuantity,
  removeCartItemById,
  selectCartItem,
  checkoutCart,
} from "../../api/cartApi";
import CheckoutModal from "./CheckoutModal";
import "./CartPage.css";

const CartPage = () => {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedItems, setSelectedItems] = useState(new Set());
  const [actionLoading, setActionLoading] = useState(new Set()); // Loading state cho từng action
  const [checkoutLoading, setCheckoutLoading] = useState(false); // Loading state cho checkout
  const [showCheckoutModal, setShowCheckoutModal] = useState(false); // State cho modal checkout
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCart = async () => {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setError("Chưa đăng nhập. Vui lòng đăng nhập lại.");
        navigate("/");
        return;
      }

      try {
        const data = await fetchCartItems(token);
        
        // Kiểm tra nếu data không phải array
        if (!Array.isArray(data)) {
          console.error("❌ Data không phải array:", data);
          setError("Dữ liệu giỏ hàng không hợp lệ");
          return;
        }
        
        // Kiểm tra nếu data rỗng
        if (data.length === 0) {
          console.log("🔍 Giỏ hàng trống");
          setCartItems([]);
          setSelectedItems(new Set());
          return;
        }
        
        const enriched = await Promise.all(
          data
            // .filter((item) => item.status === "ACTIVE")
            .map(async (item) => {
              try {
                const imageUrl = await fetchProductImage(item.productId);
                return {
                  ...item,
                  imageUrl,
                };
              } catch (imageError) {
                console.warn("⚠️ Lỗi tải ảnh sản phẩm:", imageError);
                return {
                  ...item,
                  imageUrl: "https://via.placeholder.com/150x150?text=No+Image",
                };
              }
            })
        );
        setCartItems(enriched);
        
        // Load selected items từ backend (nếu có) - dựa trên status CHECKED_OUT
        const selectedItemsFromBackend = enriched
          .filter(item => item.status === "CHECKED_OUT")
          .map(item => item.id);
        setSelectedItems(new Set(selectedItemsFromBackend));
      } catch (err) {
        console.error("❌ Lỗi load giỏ hàng:", err);
        setError(err.message || "Không thể tải giỏ hàng. Vui lòng thử lại.");
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [navigate]);

  const handleIncreaseQuantity = async (cartItemId) => {
    setActionLoading(prev => new Set(prev).add(cartItemId));
    try {
      const token = localStorage.getItem("accessToken");
      await increaseCartItemQuantity(cartItemId, token);
      setCartItems((prev) =>
        prev.map((item) =>
          item.id === cartItemId ? { ...item, quantity: item.quantity + 1 } : item
        )
      );
    } catch (err) {
      console.error("❌ Lỗi tăng số lượng:", err.message);
      // Hiển thị thông báo lỗi cho user
      alert(`Lỗi tăng số lượng: ${err.message}`);
    } finally {
      setActionLoading(prev => {
        const newSet = new Set(prev);
        newSet.delete(cartItemId);
        return newSet;
      });
    }
  };

  const handleDecreaseQuantity = async (cartItemId) => {
    setActionLoading(prev => new Set(prev).add(cartItemId));
    try {
      const token = localStorage.getItem("accessToken");
      await decreaseCartItemQuantity(cartItemId, token);
      setCartItems((prev) =>
        prev.map((item) => {
          if (item.id === cartItemId) {
            const newQuantity = item.quantity - 1;
            // Nếu số lượng = 0, xóa sản phẩm khỏi danh sách
            if (newQuantity <= 0) {
              return null; // Sẽ được filter ra
            }
            return { ...item, quantity: newQuantity };
          }
          return item;
        }).filter(Boolean) // Loại bỏ các item null
      );
      
      // Xóa khỏi selected items nếu sản phẩm bị xóa
      setSelectedItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(cartItemId);
        return newSet;
      });
    } catch (err) {
      console.error("❌ Lỗi giảm số lượng:", err.message);
      // Hiển thị thông báo lỗi cho user
      alert(`Lỗi giảm số lượng: ${err.message}`);
    } finally {
      setActionLoading(prev => {
        const newSet = new Set(prev);
        newSet.delete(cartItemId);
        return newSet;
      });
    }
  };

  const removeItem = async (cartItemId) => {
    setActionLoading(prev => new Set(prev).add(cartItemId));
    try {
      const token = localStorage.getItem("accessToken");
      await removeCartItemById(cartItemId, token);
      setCartItems((prev) => prev.filter((item) => item.id !== cartItemId));
      // Xóa khỏi selected items nếu có
      setSelectedItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(cartItemId);
        return newSet;
      });
    } catch (err) {
      console.error("❌ Lỗi xoá sản phẩm:", err.message);
      // Hiển thị thông báo lỗi cho user
      alert(`Lỗi xóa sản phẩm: ${err.message}`);
    } finally {
      setActionLoading(prev => {
        const newSet = new Set(prev);
        newSet.delete(cartItemId);
        return newSet;
      });
    }
  };

  const handleSelectItem = async (cartItemId, selected) => {
    // Tìm productId từ cartItem
    const cartItem = cartItems.find(item => item.id === cartItemId);
    if (!cartItem) {
      console.error("❌ Không tìm thấy cart item:", cartItemId);
      return;
    }

    // Cập nhật UI ngay lập tức
    setSelectedItems((prev) => {
      const newSet = new Set(prev);
      if (selected) {
        newSet.add(cartItemId);
      } else {
        newSet.delete(cartItemId);
      }
      console.log("✅ Cập nhật selectedItems:", Array.from(newSet));
      return newSet;
    });

    // Sau đó gọi API (không block UI)
    const token = localStorage.getItem("accessToken");
    if (!token) {
      console.warn("⚠️ Không có token, chỉ cập nhật local state");
      return;
    }

    selectCartItem(cartItem.productId, selected, token)
      .then(() => {
        console.log("✅ API call thành công");
      })
      .catch((err) => {
        console.error("❌ Lỗi chọn sản phẩm:", err.message);
        // Revert UI nếu API fail
        setSelectedItems((prev) => {
          const newSet = new Set(prev);
          if (selected) {
            newSet.delete(cartItemId);
          } else {
            newSet.add(cartItemId);
          }
          return newSet;
        });
        alert(`Lỗi chọn sản phẩm: ${err.message}`);
      });
  };

  const handleSelectAll = async (selectAll) => {
    // Cập nhật UI ngay lập tức
    if (selectAll) {
      setSelectedItems(new Set(cartItems.map(item => item.id)));
    } else {
      setSelectedItems(new Set());
    }

    // Sau đó gọi API (không block UI)
    const token = localStorage.getItem("accessToken");
    if (!token) {
      console.warn("⚠️ Không có token, chỉ cập nhật local state");
      return;
    }

    const promises = cartItems.map(item => 
      selectCartItem(item.productId, selectAll, token)
    );
    
    Promise.all(promises)
      .then(() => {
        console.log("✅ API selectAll thành công");
      })
      .catch((err) => {
        console.error("❌ Lỗi chọn tất cả:", err.message);
        // Revert UI nếu API fail
        if (selectAll) {
          setSelectedItems(new Set());
        } else {
          setSelectedItems(new Set(cartItems.map(item => item.id)));
        }
        alert(`Lỗi chọn tất cả: ${err.message}`);
      });
  };

  const totalPrice = cartItems.reduce(
    (sum, item) => {
      if (selectedItems.has(item.id)) {
        return sum + item.price * item.quantity;
      }
      return sum;
    },
    0
  );

  const selectedItemsCount = selectedItems.size;

  // Function để checkout giỏ hàng
  const handleCheckout = async () => {
    if (selectedItemsCount === 0) {
      alert("Vui lòng chọn ít nhất một sản phẩm để đặt hàng!");
      return;
    }

    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("Vui lòng đăng nhập để đặt hàng!");
      navigate("/login");
      return;
    }

    setCheckoutLoading(true);
    try {
      console.log("🛒 Bắt đầu checkout giỏ hàng...");
      
      // Gọi API checkout
      const result = await checkoutCart(token);
      console.log("✅ Checkout thành công:", result);
      
      // Mở modal để nhập thông tin đặt hàng
      setShowCheckoutModal(true);
      
    } catch (error) {
      console.error("❌ Lỗi checkout:", error);
      alert(`Lỗi khi đặt hàng: ${error.message}`);
    } finally {
      setCheckoutLoading(false);
    }
  };

  // Function xử lý khi checkout thành công
  const handleCheckoutSuccess = () => {
    // Chuyển hướng đến trang Order History
    navigate("/");
  };

  // Debug: Log current state (chỉ giữ lại khi cần debug)
  // console.log("🔍 Cart items count:", cartItems.length);

  return (
    <div className="cart-page-wrapper">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="header-left">
            <div className="logo" onClick={() => navigate("/")}>
              <h1>🛍️ TechShop</h1>
            </div>
            <div className="cart-title-header">
              <span className="separator">|</span>
              <span className="cart-text">Giỏ hàng</span>
            </div>
          </div>
        </div>
      </header>

      <div className="cart-page">
        <div className="container">
          {loading && (
          <div className="loading">
            <div className="spinner"></div>
            <p>Đang tải giỏ hàng...</p>
          </div>
        )}
        
        {error && (
          <div className="error-message">
            <p>{error}</p>
            <button className="btn btn-primary" onClick={() => navigate("/")}>
              Về trang chủ
            </button>
          </div>
        )}
        
        {!loading && !error && cartItems.length === 0 && (
          <div className="empty-cart">
            <div className="empty-cart-icon">🛒</div>
            <h3>Giỏ hàng trống</h3>
            <p>Bạn chưa có sản phẩm nào trong giỏ hàng</p>
            <button className="btn btn-primary" onClick={() => navigate("/")}>
              Tiếp tục mua sắm
            </button>
          </div>
        )}

        {!loading && cartItems.length > 0 && (
          <div className="cart-content">
            {/* Select All */}
            <div className="cart-select-all">
              <label className="select-all-label">
                <input
                  type="checkbox"
                  checked={selectedItems.size === cartItems.length && cartItems.length > 0}
                  onChange={(e) => handleSelectAll(e.target.checked)}
                  className="select-all-checkbox"
                  disabled={actionLoading.size > 0}
                />
                <span className="select-all-text">
                  Chọn tất cả ({cartItems.length} sản phẩm)
                  {actionLoading.size > 0 && " ⏳"}
                </span>
              </label>
            </div>
            
            <div className="cart-items">
              {cartItems.map((item) => (
                <div className="cart-item" key={item.id}>
                  <div className="cart-item-select">
                    <input
                      type="checkbox"
                      checked={selectedItems.has(item.id)}
                      onChange={(e) => {
                        console.log("🔘 Checkbox clicked:", item.id, e.target.checked);
                        handleSelectItem(item.id, e.target.checked);
                      }}
                      className="item-checkbox"
                      disabled={actionLoading.has(item.id)}
                    />
                  </div>
                  
                  <div className="cart-item-image">
                    {item.imageUrl === "/images/no-image.png" ? (
                      <div className="no-image-placeholder">📱</div>
                    ) : (
                      <img src={item.imageUrl} alt={item.productName} />
                    )}
                  </div>
                  
                  <div className="cart-item-info">
                    <h4 className="cart-item-name">{item.productName}</h4>
                    <div className="cart-item-price">
                      <span className="price">{item.price.toLocaleString('vi-VN')} ₫</span>
                    </div>
                  </div>
                  
                  <div className="cart-item-quantity">
                    <div className="quantity-control">
                      <button 
                        className="qty-btn" 
                        onClick={() => handleDecreaseQuantity(item.id)}
                        disabled={item.quantity <= 1 || actionLoading.has(item.id)}
                      >
                        {actionLoading.has(item.id) ? "⏳" : "-"}
                      </button>
                      <span className="quantity">{item.quantity}</span>
                      <button 
                        className="qty-btn" 
                        onClick={() => handleIncreaseQuantity(item.id)}
                        disabled={actionLoading.has(item.id)}
                      >
                        {actionLoading.has(item.id) ? "⏳" : "+"}
                      </button>
                    </div>
                  </div>
                  
                  <div className="cart-item-total">
                    <span className="total-price">
                      {(item.price * item.quantity).toLocaleString('vi-VN')} ₫
                    </span>
                  </div>
                  
                  <div className="cart-item-actions">
                    <button 
                      className="remove-btn"
                      onClick={() => removeItem(item.id)}
                      disabled={actionLoading.has(item.id)}
                    >
                      {actionLoading.has(item.id) ? "⏳" : "🗑️"}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
        </div>
      </div>

      {!loading && cartItems.length > 0 && (
        <div className="cart-summary-fixed">
          <div className="cart-summary">
            <div className="summary-info">
              <h3>Tổng cộng: <span className="total-amount">{totalPrice.toLocaleString('vi-VN')} ₫</span></h3>
              <p className="item-count">{selectedItemsCount} sản phẩm đã chọn</p>
            </div>
            <div className="summary-actions">
              <button className="btn btn-secondary" onClick={() => navigate("/")}>
                Tiếp tục mua sắm
              </button>
              <button 
                className="btn btn-primary checkout-btn"
                onClick={handleCheckout}
                disabled={selectedItemsCount === 0 || checkoutLoading}
              >
                {checkoutLoading ? "Đang xử lý..." : (selectedItemsCount === 0 ? "Chọn sản phẩm" : "Đặt hàng")}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Checkout Modal */}
      <CheckoutModal
        isOpen={showCheckoutModal}
        onClose={() => setShowCheckoutModal(false)}
        onSuccess={handleCheckoutSuccess}
        selectedItems={cartItems.filter(item => selectedItems.has(item.id))}
      />
    </div>
  );
};

export default CartPage;
