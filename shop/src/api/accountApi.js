import axios from "axios";
const ACCOUNT_API_BASE_URL =
  (typeof process !== "undefined" && process.env?.REACT_APP_API_BASE)
    || (typeof window !== "undefined" ? window.location.origin : "");

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