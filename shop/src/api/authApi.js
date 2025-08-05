// src/api/authApi.js
import { setRefreshing } from "../store/loadingStore"; // ở đầu file
import axios from "axios";

// ✅ [NEW] Global refresh state for UI loader
let isRefreshing = false;
export const isSessionRefreshing = () => isRefreshing;

// ✅ [1] Tạo instance axios
const authAxios = axios.create({
  baseURL: /*process.env.REACT_APP_AUTH_API ||*/ "http://localhost:9003",
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ [2] Gắn accessToken vào mọi request
authAxios.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// // ✅ [3] Auto refresh token khi gặp lỗi 401
// authAxios.interceptors.response.use(
//   (response) => response,
//   async (error) => {
//     const originalRequest = error.config;

//     // ✅ [3.1] Check lỗi 401 và chưa retry
//     if (
//       error.response?.status === 401 &&
//       !originalRequest._retry &&
//       localStorage.getItem("refreshToken")
//     ) {
//       originalRequest._retry = true;

//       try {
//         // trong interceptor:
//         console.log("⚠️ Token hết hạn, đang làm mới...");
//         setRefreshing(true);

//         // ✅ [3.2] Gửi request refresh token
//         const res = await axios.post(
//           `${authAxios.defaults.baseURL}/auth/refresh`,
//           {
//             refreshToken: localStorage.getItem("refreshToken"),
//           },
//           {
//             headers: { "Content-Type": "application/json" },
//           }
//         );

//         const { accessToken, refreshToken } = res.data.data;

//         // ✅ [3.3] Lưu lại token mới
//         localStorage.setItem("accessToken", accessToken);
//         localStorage.setItem("refreshToken", refreshToken);

//         // ✅ [3.4] Thêm token vào request gốc và gửi lại
//         originalRequest.headers.Authorization = `Bearer ${accessToken}`;
//         return authAxios(originalRequest);
//       } catch (err) {
//         localStorage.clear();
//         window.location.href = "/";
//         return Promise.reject(err);
//       } finally {
//         // ✅ [NEW] Tắt trạng thái đang làm mới phiên
//         // và trong finally:
//         setRefreshing(false);
//       }
//     }

//     return Promise.reject(error);
//   }
// );

// ✅ [4] Các API dùng chung
export const loginUser = async (data) => {
  const res = await authAxios.post("/auth/login", data);
  return res.data.data;
};

export const registerUser = async (data) => {
  const res = await authAxios.post("/auth/register", data);
  return res.data.data;
};

// export const logoutUser = () => {
//   localStorage.clear();
// };

export const getCurrentUser = async () => {
  const res = await authAxios.get("/auth/me");
  return res.data.data;
};

// src/api/authApi.js

export const logoutUser = async () => {
  try {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) throw new Error("Không có refreshToken.");

    await authAxios.post("/auth/logout", { refreshToken });

    console.log("✅ Logout thành công từ backend.");
  } catch (err) {
    console.warn("⚠️ Logout từ backend thất bại:", err?.response?.data || err.message);
  } finally {
    localStorage.clear();
    window.location.href = "/";
  }
};

