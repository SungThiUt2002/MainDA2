import { createAxiosInstance } from "./axiosInstance";

const roleAxios = createAxiosInstance({
  baseURL: "http://167.172.88.205", // Đổi port nếu cần
  headers: { "Content-Type": "application/json" },
});

// Role APIs
// Toàn bộ role APIs
export const getAllRoles = (token) =>
  roleAxios.get("/roles/allRole", { headers: { 
    Authorization: `Bearer ${token}` } });

export const createRole = (data, token) =>
  roleAxios.post("/roles/create", data, { headers: { Authorization: `Bearer ${token}` } });

export const updateRole = (id, data, token) =>
  roleAxios.put(`/roles/update/${id}`, data, { headers: { Authorization: `Bearer ${token}` } });

export const deleteRole = (id, token) =>
  roleAxios.delete(`/roles/delete/${id}`, { headers: { Authorization: `Bearer ${token}` } });

// Permission APIs
// Lấy tất cả quyền
export const getAllPermissions = (token) =>
  roleAxios.get("/permissions", { headers: { Authorization: `Bearer ${token}` } });

export const createPermission = (data, token) =>
  roleAxios.post("/permissions/create", data, { headers: { Authorization: `Bearer ${token}` } });

export const updatePermission = (id, data, token) =>
  roleAxios.put(`/permissions/update/${id}`, data, { headers: { Authorization: `Bearer ${token}` } });

export const deletePermission = (id, token) =>
  roleAxios.delete(`/permissions/delete/${id}`, { headers: { Authorization: `Bearer ${token}` } });