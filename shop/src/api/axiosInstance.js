// 📁 src/api/axiosInstance.js
import axios from "axios";

// 🔎 Hàm kiểm tra token hết hạn
export function isTokenExpired(token, bufferSeconds = 60) {
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    const exp = payload.exp * 1000;
    return Date.now() >= (exp - bufferSeconds * 1000);
  } catch (err) {
    console.error("❌ Token không hợp lệ:", err);
    return true;
  }
}

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    error ? prom.reject(error) : prom.resolve(token);
  });
  failedQueue = [];
};

// ✅ Hàm tạo instance và gắn interceptor
export function createAxiosInstance(config = {}) {
  const instance = axios.create(config);
  const ORIGIN = typeof window !== "undefined" ? window.location.origin : "";

  instance.interceptors.request.use(
    async (config) => {
      let accessToken = localStorage.getItem("accessToken");

      // ✅ Chỉ thêm Authorization header nếu có token và không hết hạn
      if (accessToken && !isTokenExpired(accessToken)) {
        config.headers.Authorization = `Bearer ${accessToken}`;
      } else if (accessToken && isTokenExpired(accessToken)) {
        try {
          const refreshToken = localStorage.getItem("refreshToken");
          const res = await axios.post(`${ORIGIN}/auth/refresh`, {
            refreshToken,
          });

          accessToken = res.data.data.accessToken;
          const newRefreshToken = res.data.data.refreshToken;

          localStorage.setItem("accessToken", accessToken);
          localStorage.setItem("refreshToken", newRefreshToken);
          
          if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
          }
        } catch (err) {
          console.error("🔐 Refresh thất bại trong request interceptor:", err);
          // ❌ Không redirect tự động nữa, chỉ log lỗi
          console.warn("⚠️ Tiếp tục request mà không có authentication");
        }
      }

      return config;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (res) => res,
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        const refreshToken = localStorage.getItem("refreshToken");

        // ✅ Nếu không có refresh token, chỉ log lỗi thay vì redirect
        if (!refreshToken) {
          console.warn("⚠️ Không có refresh token, request tiếp tục mà không authentication");
          return Promise.reject(error);
        }

        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return instance(originalRequest);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const res = await axios.post("http://localhost:9003/auth/refresh", {
            refreshToken,
          });
          
          // Use same-origin endpoint for refresh in response interceptor as well
          // (above call kept for context, replaced line below)

          const newAccessToken = res.data.data.accessToken;
          const newRefreshToken = res.data.data.refreshToken;

          localStorage.setItem("accessToken", newAccessToken);
          localStorage.setItem("refreshToken", newRefreshToken);

          instance.defaults.headers.Authorization = `Bearer ${newAccessToken}`;
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          processQueue(null, newAccessToken);
          return instance(originalRequest);
        } catch (refreshError) {
          console.error("❌ Refresh token hết hạn:", refreshError);
          processQueue(refreshError, null);
          // ❌ Không tự động logout nữa
          console.warn("⚠️ Authentication thất bại, một số tính năng có thể không hoạt động");
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }

      return Promise.reject(error);
    }
  );

  return instance;
}

// ✅ Tạo instance axios không yêu cầu authentication
export function createPublicAxiosInstance(config = {}) {
  const instance = axios.create(config);
  
  // Không thêm bất kỳ interceptor authentication nào
  return instance;
}

// ✅ Logout và redirect về /login
function logoutAndRedirect() {
  localStorage.clear();
  window.location.href = "/";
}
