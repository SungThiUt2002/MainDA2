// // // File: src/api/cartApi.js
// // import axios from "axios";
// import axios from "./axiosInstance";

// export const addToCart = async (data) => {
//   const token = localStorage.getItem("accessToken");
//   if (!token) {
//     throw new Error("❌ Người dùng chưa đăng nhập hoặc token đã hết hạn.");
//   }

//   try {
//     const response = await axios.post(
//       "http://localhost:9008/api/carts/item",
//       data,
//       {
//         headers: {
//         //   Authorization: `Bearer ${token}`,
//           "Content-Type": "application/json",
//         },
//       }
//     );
//     return response.data;
//   } catch (err) {
//     console.error("❌ Lỗi API addToCart:", err?.response?.data || err.message);
//     throw err;
//   }
// };

// File: src/api/cartApi.js
// import axios from "./axiosInstance";

// export const addToCart = async (data) => {
//   try {
//     const response = await axios.post(
//       "http://localhost:9008/api/carts/item",
//       data,
//       {
//         headers: {
//           "Content-Type": "application/json", // Chỉ cần cái này thôi
//         },
//       }
//     );
//     return response.data;
//   } catch (err) {
//     console.error("❌ Lỗi API addToCart:", err?.response?.data || err.message);
//     throw err;
//   }
// };
// api/cartApi.js
import { createAxiosInstance, createPublicAxiosInstance } from "./axiosInstance";

// ✅ Instance với authentication (cho user đã đăng nhập)
const axiosCart = createAxiosInstance({
  baseURL: "http://localhost:9008/api/carts",
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ Instance không yêu cầu authentication (cho guest users)
const axiosCartPublic = createPublicAxiosInstance({
  baseURL: "http://localhost:9008/api/carts",
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ Gọi API: Thêm sản phẩm vào giỏ (hỗ trợ cả có và không có token)
export const addToCart = async (data, token = null) => {
  try {
    // ✅ Sử dụng instance phù hợp tùy vào có token hay không
    const axiosInstance = token ? axiosCart : axiosCartPublic;
    const res = await axiosInstance.post("/item", data);
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API addToCart:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Gọi API: Lấy danh sách item trong giỏ (hỗ trợ cả có và không có token)
export const fetchCartItems = async (token = null) => {
  try {
    // ✅ Sử dụng instance phù hợp tùy vào có token hay không
    const axiosInstance = token ? axiosCart : axiosCartPublic;
    const res = await axiosInstance.get("/items");
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi API fetchCartItems:", err?.response?.data || err.message);
    throw err;
  }
};

// ✅ Gọi API: Lấy ảnh thumbnail theo productId
export const fetchProductImage = async (productId) => {
  const fallback = "https://via.placeholder.com/150x150?text=No+Image";

  if (!productId) return fallback;

  try {
    // Sửa lại endpoint để khớp với controller
    const res = await fetch(`http://localhost:9001/api/v1/product-images/product/${productId}/thumbnail`);
    
    if (!res.ok) {
      console.warn(`❌ Không tìm thấy ảnh thumbnail cho product ${productId}:`, res.status);
      return fallback;
    }

    // Kiểm tra content-type để đảm bảo response là JSON
    const contentType = res.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      console.warn(`❌ Response không phải JSON cho product ${productId}:`, contentType);
      return fallback;
    }

    // Kiểm tra response có dữ liệu không
    const text = await res.text();
    if (!text || text.trim() === "") {
      console.warn(`❌ Response rỗng cho product ${productId}`);
      return fallback;
    }

    const data = JSON.parse(text);
    
    if (data?.url) {
      // Tạo URL đầy đủ cho ảnh
      const imageUrl = `http://localhost:9001/images/${data.url}`;
      return imageUrl;
    } else {
      console.warn("❌ Không có URL trong response:", data);
      return fallback;
    }
  } catch (err) {
    console.warn("❌ Lỗi tải ảnh sản phẩm:", err);
    return fallback;
  }
};

// 📁 Tăng số lượng sản phẩm lên 1
export const increaseCartItemQuantity = async (cartItemId) => {
  try {
    const res = await axiosCart.put(`/increase-quantity/${cartItemId}`, {});
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi increaseCartItemQuantity:", err.response?.data || err.message);
    throw err;
  }
};

// Giảm số lượng xuống 1
export const decreaseCartItemQuantity = async (cartItemId) => {
  try {
    const res = await axiosCart.put(`/decrease-quantity/${cartItemId}`, {});
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi decreaseCartItemQuantity:", err.response?.data || err.message);
    throw err;
  }
};


// Xóa sản phẩm khỏi giỏ hàng
export const removeCartItemById = async (cartItemId, token) => {
  try {
    await axiosCart.delete(`/item/delete/${cartItemId}`);
  } catch (err) {
    console.error("❌ Lỗi API xóa sản phẩm:", err.response?.data || err);
    throw err;
  }
};

// ✅ Checkout giỏ hàng
export const checkoutCart = async (token) => {
  try {
    const res = await axiosCart.post("/checkout", {});
    return res.data;
  } catch (err) {
    console.error("❌ Lỗi checkout cart:", err.response?.data || err.message);
    throw err;
  }
};

// ✅ Chọn/bỏ chọn sản phẩm trong giỏ hàng
export const selectCartItem = async (productId, selected, token) => {
  try {
    const response = await axiosCart.put(`/item/${productId}/select?selected=${selected}`, {});
    return response;
  } catch (err) {
    console.error("❌ Lỗi API selectCartItem:", err.response?.data || err);
    throw err;
  }
};
