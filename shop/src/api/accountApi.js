import axios from "axios";
const ACCOUNT_API_BASE_URL = "http://167.172.88.205"; // Đổi port nếu khác

const accountAxios = axios.create({
  baseURL: ACCOUNT_API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});



export const getAllUsers = (token) =>
  accountAxios.get("/users/admin/allUser", {
    headers: { Authorization: `Bearer ${token}` },
  });

export const getUserById = (id, token) =>
  accountAxios.get(`/users/admin/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

export const createUser = (user, token) =>
  accountAxios.post("/users/admin/create-user-with-roles", user, {
    headers: { Authorization: `Bearer ${token}` },
  });

// Không có updateUser endpoint chuẩn, tạm thời bỏ
// export const updateUser = ...

export const deleteUser = (id, token) =>
  accountAxios.delete(`/users/delete/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  }); 