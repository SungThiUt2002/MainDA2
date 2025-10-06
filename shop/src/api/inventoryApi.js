import { createAxiosInstance } from './axiosInstance';
import { getAllProducts } from './productApi';

const inventoryApi = createAxiosInstance({
  baseURL: (typeof window !== 'undefined' ? window.location.origin : '') + '/api/v1/inventory-items',
});



// Lấy thông tin tồn kho của một sản phẩm theo productId
export const getInventoryByProductId = async (productId) => {
  try {
    const response = await inventoryApi.get(`/product/${productId}`);
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy thông tin tồn kho:', error);
    throw error;
  }
};

// Lấy số lượng có sẵn của một sản phẩm
export const getAvailableQuantity = async (productId) => {
  try {
    const response = await inventoryApi.get(`/products/${productId}/available-quantity`);
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy số lượng có sẵn:', error);
    throw error;
  }
};

// Lấy số lượng đã bán của một sản phẩm
export const getSoldQuantity = async (productId) => {
  try {
    const response = await inventoryApi.get(`/products/${productId}/sold-quantity`);
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy số lượng đã bán:', error);
    throw error;
  }
};

// Lấy tất cả thông tin tồn kho
export const getAllInventoryItems = async () => {
  try {
    // Gọi trực tiếp API inventory service để lấy dữ liệu mới nhất
    console.log('🔄 Lấy danh sách inventory từ inventory service...');
    const response = await inventoryApi.get('/all');
    console.log('📦 Inventory service response:', response);
    
    const inventoryItems = response.data || response;
    console.log('📦 Inventory data:', inventoryItems);
    console.log('📦 Inventory count:', inventoryItems.length);
    
    return inventoryItems;
    
  } catch (error) {
    console.error('❌ Lỗi khi lấy inventory từ inventory service:', error);
    console.log('❌ Không thể lấy dữ liệu inventory');
    return { data: [] };
  }
};

// Lấy danh sách sản phẩm có sẵn
export const getAvailableInventoryItems = async () => {
  try {
    const response = await inventoryApi.get('/available');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm có sẵn:', error);
    throw error;
  }
};

// Lấy danh sách sản phẩm sắp hết hàng
export const getLowStockItems = async () => {
  try {
    const response = await inventoryApi.get('/low-stock');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm sắp hết hàng:', error);
    throw error;
  }
};

// Lấy danh sách sản phẩm cần đặt hàng lại
export const getItemsNeedingReorder = async () => {
  try {
    const response = await inventoryApi.get('/needing-reorder');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm cần đặt hàng:', error);
    throw error;
  }
};

// Nhập kho cho sản phẩm
export const importStock = async (productId, stockData, token) => {
  try {
    console.log('Importing stock with data:', { productId, stockData, token: token ? 'Bearer ***' : 'No token' });
    
    // Gọi API inventory service để nhập kho
    console.log('🔄 Gọi API inventory service để nhập kho...');
    const response = await inventoryApi.post(`/${productId}`, stockData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    console.log('✅ Đã nhập kho thành công:', response);
    return response;
  } catch (error) {
    console.error('Lỗi khi nhập kho:', error);
    console.error('Error response:', error.response?.data);
    console.error('Error status:', error.response?.status);
    throw error;
  }
}; 