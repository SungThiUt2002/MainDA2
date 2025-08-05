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

  instance.interceptors.request.use(
    async (config) => {
      let accessToken = localStorage.getItem("accessToken");

      if (isTokenExpired(accessToken)) {
        try {
          const refreshToken = localStorage.getItem("refreshToken");
          const res = await axios.post("http://localhost:9003/auth/refresh", {
            refreshToken,
          });

          accessToken = res.data.data.accessToken;
          const newRefreshToken = res.data.data.refreshToken;

          localStorage.setItem("accessToken", accessToken);
          localStorage.setItem("refreshToken", newRefreshToken);
        } catch (err) {
          console.error("🔐 Refresh thất bại trong request interceptor:", err);
          logoutAndRedirect(); // 🔧 Thêm gọi logout
          return Promise.reject(err);
        }
      }

      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
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

        if (!refreshToken) {
          logoutAndRedirect();
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
          logoutAndRedirect(); // 🔧 Gọi logout tại đây
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

// ✅ Logout và redirect về /login
function logoutAndRedirect() {
  localStorage.clear();
  window.location.href = "/";
}
