import React, { useState, useEffect, useCallback } from "react";
import { getLatestOrder, updateOrderInfo, confirmOrder, getPaymentMethods, getUserAddresses, createAddress } from "../../api/orderApi";
import SuccessNotification from "../../components/SuccessNotification";
import "./CheckoutModal.css";

const CheckoutModal = ({ isOpen, onClose, onSuccess, selectedItems }) => {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [formData, setFormData] = useState({
    addressId: "",
    phoneNumber: "",
    paymentMethod: "COD", // Mặc định thanh toán khi nhận hàng
    note: ""
  });
  const [userAddresses, setUserAddresses] = useState([]);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [newAddressData, setNewAddressData] = useState({
    receiverName: "",
    receiverPhone: "",
    province: "",
    district: "",
    ward: "",
    streetAddress: ""
  });
  const [showSuccessNotification, setShowSuccessNotification] = useState(false);

  const fetchLatestOrder = useCallback(async (retryCount = 0) => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem("accessToken");
      console.log("🔍 Fetching latest order with token:", token ? "exists" : "missing");
      console.log("🔍 Retry count:", retryCount);
      
      // Thêm delay nếu đây là lần thử đầu tiên (đợi đơn hàng được tạo)
      if (retryCount === 0) {
        console.log("⏳ Đợi 2 giây để đơn hàng được tạo...");
        await new Promise(resolve => setTimeout(resolve, 2000));
      }
      
      const orderData = await getLatestOrder(token);
      console.log("✅ Received order data:", orderData);
      console.log("✅ Order ID from API:", orderData?.id);
      
      // Sử dụng dữ liệu từ API để đảm bảo tính nhất quán với backend
      console.log("🔍 Sử dụng dữ liệu từ API getLatestOrder()");
      console.log("✅ Order data từ API:", orderData);
      console.log("✅ Order items từ API:", orderData?.items);
      
      // Không override với selectedItems, chỉ sử dụng dữ liệu từ API
      const finalOrderData = orderData;
      
      setOrder(finalOrderData);
      
      // Pre-fill form với thông tin có sẵn
      if (finalOrderData) {
        setFormData(prev => ({
          ...prev,
          shippingAddress: finalOrderData.shippingAddress || "",
          phoneNumber: finalOrderData.phoneNumber || ""
        }));
      }
    } catch (err) {
      console.error("❌ Lỗi lấy đơn hàng:", err);
      
      // Retry logic - thử lại tối đa 3 lần
      if (retryCount < 3) {
        console.log(`🔄 Thử lại lần ${retryCount + 1}/3...`);
        setTimeout(() => {
          fetchLatestOrder(retryCount + 1);
        }, 1000 * (retryCount + 1)); // Tăng delay mỗi lần retry
        return;
      }
      
      setError("Không thể lấy thông tin đơn hàng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchPaymentMethods = useCallback(async () => {
    try {
      const methods = await getPaymentMethods();
      setPaymentMethods(methods);
    } catch (err) {
      console.error("❌ Lỗi lấy phương thức thanh toán:", err);
      // Fallback về danh sách mặc định nếu API lỗi
      setPaymentMethods([
        { value: "COD", displayName: "Thanh toán khi nhận hàng (COD)" },
        { value: "BANK_TRANSFER", displayName: "Chuyển khoản ngân hàng" },
        { value: "VNPAY", displayName: "Thanh toán qua VNPAY" },
        { value: "MOMO", displayName: "Thanh toán qua MOMO" },
        { value: "PAYPAL", displayName: "Thanh toán qua PayPal" }
      ]);
    }
  }, []);

  const fetchUserAddresses = useCallback(async () => {
    try {
      console.log("🔄 Đang lấy danh sách địa chỉ...");
      const addresses = await getUserAddresses();
      console.log("✅ Danh sách địa chỉ:", addresses);
      console.log("✅ Số lượng địa chỉ:", addresses?.length || 0);
      setUserAddresses(addresses || []);
    } catch (err) {
      console.error("❌ Lỗi lấy danh sách địa chỉ:", err);
      setUserAddresses([]);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      // Force clear any cached data
      setOrder(null);
      setFormData({
        addressId: "",
        phoneNumber: "",
        paymentMethod: "COD",
        note: ""
      });
      
      // Debug: Log API approach
      console.log("🔍 CheckoutModal - Sử dụng dữ liệu từ API getLatestOrder()");
      console.log("🔍 CheckoutModal - selectedItems được truyền nhưng không sử dụng:", selectedItems?.length || 0);
      
      // Fetch fresh data
      fetchLatestOrder();
      fetchPaymentMethods();
      fetchUserAddresses();
    }
  }, [isOpen, fetchLatestOrder, fetchPaymentMethods, fetchUserAddresses]);

  const handleCreateAddress = async () => {
    try {
      const newAddress = await createAddress(newAddressData);
      setUserAddresses(prev => [...prev, newAddress]);
      setShowAddressForm(false);
      setNewAddressData({
        receiverName: "",
        receiverPhone: "",
        province: "",
        district: "",
        ward: "",
        streetAddress: ""
      });
    } catch (err) {
      console.error("❌ Lỗi tạo địa chỉ mới:", err);
      setError("Không thể tạo địa chỉ mới. Vui lòng thử lại.");
    }
  };

  const handleNewAddressInputChange = (e) => {
    const { name, value } = e.target;
    setNewAddressData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const getPaymentMethodIcon = (method) => {
    switch (method) {
      case "COD":
        return "💵";
      case "BANK_TRANSFER":
        return "🏦";
      case "VNPAY":
        return "💳";
      case "MOMO":
        return "📱";
      case "PAYPAL":
        return "🌐";
      default:
        return "💳";
    }
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
    
    if (!order) {
      setError("Không có thông tin đơn hàng");
      return;
    }

    // Validation
    if (!formData.addressId) {
      setError("Vui lòng chọn địa chỉ giao hàng");
      return;
    }

    console.log("🔍 Form data trước khi gửi:", formData);
    console.log("🔍 Selected addressId:", formData.addressId);
    console.log("🔍 Available addresses:", userAddresses);

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("accessToken");
      
      // Cập nhật thông tin đơn hàng
      await updateOrderInfo(order.id, formData, token);
      
      // Xác nhận đơn hàng
      await confirmOrder(order.id, token);
      
      // Hiển thị thông báo thành công
      setShowSuccessNotification(true);
      
      // Đóng modal và callback sau 2 giây
      setTimeout(() => {
        onSuccess();
        onClose();
      }, 2000);
      
    } catch (err) {
      console.error("❌ Lỗi xử lý đơn hàng:", err);
      setError(err.message || "Có lỗi xảy ra khi xử lý đơn hàng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (window.confirm("Bạn có chắc muốn hủy đơn hàng này?")) {
      onClose();
    }
  };



  if (!isOpen) return null;

  return (
    <div className="checkout-modal-overlay">
      <div className="checkout-modal">
        <div className="checkout-modal-header">
          <h2>🛒 Hoàn tất đặt hàng</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

                 {loading && !order && (
           <div className="loading-container">
             <div className="spinner"></div>
             <p>Đang tạo và tải thông tin đơn hàng...</p>
             <p style={{ fontSize: '12px', color: '#666', marginTop: '10px' }}>
               Vui lòng đợi trong giây lát...
             </p>
           </div>
         )}

                 {error && (
           <div className="error-message">
             <p>{error}</p>
             <p style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>
               Có thể đơn hàng đang được tạo, vui lòng thử lại sau vài giây.
             </p>
             <button className="btn btn-secondary" onClick={() => fetchLatestOrder(0)}>
               🔄 Thử lại
             </button>
           </div>
         )}



        {order && (
          <div className="checkout-modal-content">

            
            {/* Thông tin đơn hàng */}
            <div className="order-info">
              <h3>Thông tin đơn hàng #{order.id}</h3>
              {console.log("🔍 Rendering order with ID:", order.id)}
              <div className="order-summary-simple">
                <div className="summary-row">
                  <span>Tổng tiền:</span>
                  <span className="total-amount">{order.totalAmount?.toLocaleString('vi-VN')} ₫</span>
                </div>
                <div className="summary-row">
                  <span>Số sản phẩm:</span>
                  <span>{order.items?.length || 0} sản phẩm</span>
                </div>
              </div>
            </div>

                         {/* Danh sách sản phẩm */}
             {order.items && order.items.length > 0 ? (
               <div className="products-section">
                 <h3>Danh sách sản phẩm</h3>
                 <div className="products-list">
                   {order.items.map((item, index) => {
                     console.log(`🔍 Rendering item ${index}:`, {
                       productId: item.productId,
                       productName: item.productName,
                       price: item.productPrice || item.price,
                       quantity: item.quantity,
                       totalPrice: item.totalPrice
                     });
                     
                     const productName = item.productName || `Sản phẩm #${item.productId}`;
                     const price = item.productPrice || item.price;
                     const totalPrice = item.totalPrice || (price * item.quantity);
                     
                     return (
                       <div key={index} className="product-item">
                         <div className="product-name">{productName}</div>
                         <div className="product-details">
                           <span>Số lượng: {item.quantity}</span>
                           <span>Giá: {price?.toLocaleString('vi-VN')} ₫</span>
                           <span className="item-total">Tổng: {totalPrice?.toLocaleString('vi-VN')} ₫</span>
                         </div>
                       </div>
                     );
                   })}
                 </div>
               </div>
             ) : (
               <div className="products-section">
                 <h3>Không có sản phẩm</h3>
                 <p>Không tìm thấy sản phẩm trong đơn hàng.</p>
               </div>
             )}

                         {/* Form thông tin giao hàng */}
             <form onSubmit={handleSubmit} className="checkout-form">
               <h3>Thông tin giao hàng</h3>
               
               <div className="form-row">
                 <div className="form-group">
                   <label htmlFor="addressId">Chọn địa chỉ giao hàng *</label>
                                       {(() => {
                      console.log("🔍 Render - userAddresses:", userAddresses);
                      console.log("🔍 Render - userAddresses.length:", userAddresses?.length);
                      
                      return userAddresses && userAddresses.length > 0 ? (
                        <select
                          id="addressId"
                          name="addressId"
                          value={formData.addressId}
                          onChange={handleInputChange}
                          required
                        >
                          <option value="">-- Chọn địa chỉ --</option>
                          {userAddresses.map((address, index) => (
                            <option key={address.id || index} value={address.id}>
                              {address.receiverName} - {address.receiverPhone} - {`${address.streetAddress}, ${address.ward}, ${address.district}, ${address.province}`}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <div className="no-addresses">
                          <p>Bạn chưa có địa chỉ nào. Vui lòng tạo địa chỉ mới.</p>
                        </div>
                      );
                    })()}
                   
                   <button
                     type="button"
                     className="btn btn-secondary"
                     onClick={() => setShowAddressForm(true)}
                     style={{ marginTop: '10px' }}
                   >
                     ➕ Thêm địa chỉ mới
                   </button>
                 </div>
               </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="paymentMethod">Phương thức thanh toán</label>
                                     <select
                     id="paymentMethod"
                     name="paymentMethod"
                     value={formData.paymentMethod}
                     onChange={handleInputChange}
                   >
                     {paymentMethods.map((method) => (
                       <option key={method.value} value={method.value}>
                         {getPaymentMethodIcon(method.value)} {method.displayName}
                       </option>
                     ))}
                   </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="note">Ghi chú</label>
                  <textarea
                    id="note"
                    name="note"
                    value={formData.note}
                    onChange={handleInputChange}
                    placeholder="Ghi chú thêm cho đơn hàng (không bắt buộc)..."
                    rows="2"
                  />
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCancel}
                  disabled={loading}
                >
                  ❌ Hủy đơn hàng
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? "⏳ Đang xử lý..." : "✅ Xác nhận đặt hàng"}
                </button>
              </div>
            </form>
          </div>
        )}

                 {/* Success Notification */}
         <SuccessNotification
           message="🎉 Đặt hàng thành công! Đơn hàng của bạn đã được xác nhận."
           isVisible={showSuccessNotification}
           onClose={() => setShowSuccessNotification(false)}
           duration={3000}
         />

         {/* Address Creation Modal */}
         {showAddressForm && (
           <div className="address-modal-overlay">
             <div className="address-modal">
               <div className="address-modal-header">
                 <h3>📍 Thêm địa chỉ mới</h3>
                 <button className="close-btn" onClick={() => setShowAddressForm(false)}>×</button>
               </div>
               
               <div className="address-modal-content">
                 <form onSubmit={(e) => { e.preventDefault(); handleCreateAddress(); }}>
                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="receiverName">Tên người nhận *</label>
                       <input
                         type="text"
                         id="receiverName"
                         name="receiverName"
                         value={newAddressData.receiverName}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập tên người nhận..."
                         required
                       />
                     </div>
                   </div>

                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="receiverPhone">Số điện thoại *</label>
                       <input
                         type="tel"
                         id="receiverPhone"
                         name="receiverPhone"
                         value={newAddressData.receiverPhone}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập số điện thoại..."
                         required
                       />
                     </div>
                   </div>

                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="province">Tỉnh/Thành phố *</label>
                       <input
                         type="text"
                         id="province"
                         name="province"
                         value={newAddressData.province}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập tỉnh/thành phố..."
                         required
                       />
                     </div>
                   </div>

                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="district">Quận/Huyện *</label>
                       <input
                         type="text"
                         id="district"
                         name="district"
                         value={newAddressData.district}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập quận/huyện..."
                         required
                       />
                     </div>
                   </div>

                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="ward">Phường/Xã *</label>
                       <input
                         type="text"
                         id="ward"
                         name="ward"
                         value={newAddressData.ward}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập phường/xã..."
                         required
                       />
                     </div>
                   </div>

                   <div className="form-row">
                     <div className="form-group">
                       <label htmlFor="streetAddress">Địa chỉ chi tiết *</label>
                       <textarea
                         id="streetAddress"
                         name="streetAddress"
                         value={newAddressData.streetAddress}
                         onChange={handleNewAddressInputChange}
                         placeholder="Nhập địa chỉ chi tiết (số nhà, đường, thôn, xóm)..."
                         required
                         rows="3"
                       />
                     </div>
                   </div>

                   <div className="form-actions">
                     <button
                       type="button"
                       className="btn btn-secondary"
                       onClick={() => setShowAddressForm(false)}
                     >
                       ❌ Hủy
                     </button>
                     <button
                       type="submit"
                       className="btn btn-primary"
                     >
                       ✅ Lưu địa chỉ
                     </button>
                   </div>
                 </form>
               </div>
             </div>
           </div>
         )}
       </div>
     </div>
   );
 };

export default CheckoutModal; 