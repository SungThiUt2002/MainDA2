// src/api/productApi.js
import axios from "axios";

// Cấu hình baseURL same-origin (ưu tiên env nếu có)
const PRODUCT_API_BASE_URL =
  (typeof process !== "undefined" && process.env?.REACT_APP_API_BASE)
    || (typeof window !== "undefined" ? window.location.origin : "");

// Tạo instance riêng cho Product Service
const productAxios = axios.create({
  baseURL: PRODUCT_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
  },
});

export const createProduct = (data, token) =>
  productAxios.post("/api/v1/products", data, {
    headers: { Authorization: `Bearer ${token}` }
  });

export const updateProduct = (id, data, token) =>
  productAxios.put(`/api/v1/products/${id}`, data, {
    headers: { Authorization: `Bearer ${token}` }
  });

export const deleteProduct = (id, token) =>
  productAxios.delete(`/api/v1/products/${id}`, {
    headers: { Authorization: `Bearer ${token}` }
  });

// Lấy tất cả sản phẩm
export const getAllProducts = async () => {
  const res = await productAxios.get("/api/v1/products");
  return res;
};

// Lấy chi tiết 1 sản phẩm theo ID
export const getProductById = async (id) => {
  const res = await productAxios.get(`/api/v1/products/${id}`);
  return res;
};

// Lọc sản phẩm theo category
export const getProductsByCategory = async (categoryId) => {
  const res = await productAxios.get(`/api/v1/products/byCategory/${categoryId}`);
  return res;
};

// Categories API
export const getAllCategories = async () => {
  try {
    const response = await productAxios.get('/api/v1/categories/allCategory');
    return response.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    throw error;
  }
};

export const createCategory = async (data, token) => {
  try {
    const response = await productAxios.post('/api/v1/categories', data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error creating category:', error);
    throw error;
  }
};

export const updateCategory = async (id, data, token) => {
  try {
    const response = await productAxios.put(`/api/v1/categories/${id}`, data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating category:', error);
    throw error;
  }
};

export const deleteCategory = async (id, token) => {
  try {
    const response = await productAxios.delete(`/api/v1/categories/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting category:', error);
    throw error;
  }
};

// Brands API
export const getAllBrands = async () => {
  try {
    const response = await productAxios.get('/api/v1/brands');
    return response.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
    throw error;
  }
};

export const createBrand = async (data, token) => {
  try {
    const response = await productAxios.post('/api/v1/brands', data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error creating brand:', error);
    throw error;
  }
};

export const updateBrand = async (id, data, token) => {
  try {
    const response = await productAxios.put(`/api/v1/brands/${id}`, data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating brand:', error);
    throw error;
  }
};

export const deleteBrand = async (id, token) => {
  try {
    const response = await productAxios.delete(`/api/v1/brands/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting brand:', error);
    throw error;
  }
};

// Product Images API - Sửa endpoint cho đúng với backend
export const getProductImages = (productId) =>
  productAxios.get(`/api/v1/product-images/product/${productId}`);

// Upload file thực tế - Sử dụng endpoint đúng
export const addProductImage = (formData, token) => {
  return productAxios.post("/api/v1/product-images/upload", formData, {
    headers: { 
      Authorization: `Bearer ${token}`,
      "Content-Type": "multipart/form-data" // Quan trọng cho upload file
    }
  });
};

// Cập nhật thông tin ảnh
export const updateProductImage = (imageId, data, token) => {
  return productAxios.put(`/api/v1/product-images/update/${imageId}`, data, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Xóa ảnh
export const deleteProductImage = (imageId, token) => {
  return productAxios.delete(`/api/v1/product-images/delete/${imageId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Đặt ảnh làm thumbnail
export const setProductThumbnail = (productId, imageId, token) => {
  return productAxios.put(`/api/v1/product-images/product/${productId}/thumbnail/${imageId}`, {}, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Lấy ảnh thumbnail của sản phẩm
export const getProductThumbnail = (productId) => {
  return productAxios.get(`/api/v1/product-images/product/${productId}/thumbnail`);
};

//Đếm số ảnh của sản phẩm
export const countProductImages = (productId) => {
  return productAxios.get(`/api/v1/product-images/product/${productId}/count`);
};

// ==================== 🔍 ADVANCED SEARCH & FILTER API ====================

/**
 *  Tìm kiếm sản phẩm với advanced filters và pagination
 */
export const searchProducts = (searchParams) => {
  return productAxios.get("/api/v1/products/search", { params: searchParams });
};

/**
 * Tìm kiếm đơn giản theo keyword
 */
export const searchByKeyword = (keyword) => {
  return productAxios.get("/api/v1/products/search/keyword", { 
    params: { keyword } 
  });
};

/**
 * Lấy danh sách categories cho filter dropdown
 */
export const getDistinctCategories = () => {
  return productAxios.get("/api/v1/products/filters/categories");
};

/**
 * Lấy danh sách brands cho filter dropdown
 */
export const getDistinctBrands = () => {
  return productAxios.get("/api/v1/products/filters/brands");
};

/**
 * Lấy khoảng giá min-max cho price range filter
 */
export const getPriceRange = () => {
  return productAxios.get("/api/v1/products/filters/price-range");
};

// ==================== 📊 DASHBOARD STATISTICS API ====================

/**
 * Lấy tổng số lượng sản phẩm cho dashboard
 */
export const getTotalProductCount = () => {
  return productAxios.get("/api/v1/products/stats/count");
};