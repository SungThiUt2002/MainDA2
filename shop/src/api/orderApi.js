// File: src/api/orderApi.js
import { createAxiosInstance } from "./axiosInstance";

const axiosOrder = createAxiosInstance({
  baseURL: "http://localhost:9011/apis/v1/users/orders",
  headers: {
    "Content-Type": "application/json",
  },
});

const axiosOrderAdmin = createAxiosInstance({
  baseURL: "http://localhost:9011/apis/v1/admin/orders",
  headers: {
    "Content-Type": "application/json",
  },
});

const axiosAccount = createAxiosInstance({
  baseURL: "http://localhost:9003/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ Lấy đơn hàng mới nhất sau khi checkout
export const getLatestOrder = async (token) => {
  try {
    // Thêm timestamp để tránh cache
    const timestamp = Date.now();
    const res = await axiosOrder.get(`/latest?t=${timestamp}`);
    console.log("🔍 API call timestamp:", timestamp);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getLatestOrder:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Lấy tất cả đơn hàng của user (cho lịch sử đơn hàng)
export const getUserOrders = async (token) => {
  try {
    const res = await axiosOrder.get("/history");
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getUserOrders:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Lấy đơn hàng của user theo trạng thái
export const getUserOrdersByStatus = async (status, token) => {
  try {
    const res = await axiosOrder.get(`/history/status/${status}`);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getUserOrdersByStatus:", err?.response?.data || err.message);
    throw err;
  }
};

// ========== ADMIN ORDER APIs ==========

// Lấy tất cả đơn hàng chờ xác nhận (PENDING_CONFIRMATION)
export const getPendingConfirmationOrders = async (token) => {
  try {
    const res = await axiosOrderAdmin.get("/pending-confirmation", {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getPendingConfirmationOrders:", err?.response?.data || err.message);
    throw err;
  }
};

// Lấy chi tiết đơn hàng theo ID
export const getOrderById = async (orderId, token) => {
  try {
    const res = await axiosOrderAdmin.get(`/${orderId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getOrderById:", err?.response?.data || err.message);
    throw err;
  }
};

// Lấy đơn hàng theo trạng thái
export const getOrdersByStatus = async (status, token) => {
  try {
    const res = await axiosOrderAdmin.get(`/status/${status}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getOrdersByStatus:", err?.response?.data || err.message);
    throw err;
  }
};

// Lấy tất cả đơn hàng
export const getAllOrders = async (token) => {
  try {
    const res = await axiosOrderAdmin.get("/all", {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getAllOrders:", err?.response?.data || err.message);
    throw err;
  }
};

// Admin xác nhận đơn hàng COD
export const adminConfirmOrder = async (orderId, token) => {
  try {
    const res = await axiosOrderAdmin.put(`/${orderId}/confirm`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API adminConfirmOrder:", err?.response?.data || err.message);
    throw err;
  }
};

// Lấy tổng số đơn hàng - cho Admin Dashboard
export const getTotalOrderCount = async (token) => {
  try {
    console.log("🔄 Gọi API getTotalOrderCount với token:", token ? "Có token" : "Không có token");
    console.log("🔄 URL đầy đủ:", "http://localhost:9011/apis/v1/admin/orders/count");
    
    // Không cần thêm Authorization header vì axiosInstance đã tự động thêm
    const res = await axiosOrderAdmin.get("/count");
    
    console.log("✅ Response từ API getTotalOrderCount:", res.data);
    console.log("✅ Response structure:", JSON.stringify(res.data, null, 2));
    console.log("✅ Response status:", res.status);
    console.log("✅ Response headers:", res.headers);
    
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getTotalOrderCount:", err?.response?.data || err.message);
    console.error("❌ Error status:", err?.response?.status);
    console.error("❌ Error details:", err);
    throw err;
  }
};

// ✅ Lấy danh sách phương thức thanh toán
export const getPaymentMethods = async () => {
  try {
    const res = await axiosOrder.get("/payment-methods");
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getPaymentMethods:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Lấy danh sách địa chỉ của user từ account-service
export const getUserAddresses = async () => {
  try {
    console.log("🔄 Gọi API getUserAddresses...");
    
    // Lấy userId từ token
    const token = localStorage.getItem("accessToken");
    if (!token) {
      throw new Error("Không có token");
    }
    
    // Decode JWT token để lấy userId
    let userId;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      userId = payload.userId;
      
      console.log("🔄 Token payload:", payload);
      console.log("🔄 UserId từ token:", userId);
      
      if (!userId) {
        throw new Error("Không tìm thấy userId trong token");
      }
    } catch (decodeError) {
      console.error("❌ Lỗi decode token:", decodeError);
      throw new Error("Token không hợp lệ");
    }
    
    const res = await axiosAccount.get(`/address/all/${userId}`);
    console.log("✅ Response từ API getUserAddresses:", res.data);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getUserAddresses:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Tạo địa chỉ mới từ account-service
export const createAddress = async (addressData) => {
  try {
    console.log("🔄 Gọi API createAddress...");
    
    // Lấy token để gửi trong header
    const token = localStorage.getItem("accessToken");
    if (!token) {
      throw new Error("Không có token");
    }
    
    const res = await axiosAccount.post("/address", addressData);
    console.log("✅ Response từ API createAddress:", res.data);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API createAddress:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Cập nhật thông tin đơn hàng (địa chỉ giao hàng, phương thức thanh toán)
export const updateOrderInfo = async (orderId, orderInfo, token) => {
  try {
    const res = await axiosOrder.put(`/${orderId}/info`, orderInfo);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API updateOrderInfo:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Xác nhận đơn hàng
export const confirmOrder = async (orderId, token) => {
  try {
    await axiosOrder.put(`/${orderId}/confirm`, {});
  } catch (err) {
    console.error("❌ Lỗi API confirmOrder:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Hủy đơn hàng
export const cancelOrder = async (orderId, token) => {
  try {
    await axiosOrder.put(`/${orderId}/cancel`, {});
  } catch (err) {
    console.error("❌ Lỗi API cancelOrder:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Cập nhật trạng thái vận chuyển (Admin)
export const updateShippingStatus = async (orderId, statusData, token) => {
  try {
    const res = await axiosOrderAdmin.put(`/${orderId}/shipping-status`, statusData, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API updateShippingStatus:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Lấy danh sách trạng thái đơn hàng
export const getOrderStatuses = async (token) => {
  try {
    const res = await axiosOrderAdmin.get("/list/statuses", {
      headers: { Authorization: `Bearer ${token}` }
    });
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API getOrderStatuses:", err?.response?.data || err.message);
    throw err;
  }
}; 