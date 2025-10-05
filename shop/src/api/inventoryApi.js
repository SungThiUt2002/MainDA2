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
    // Gọi trực tiếp API products thay vì inventory
    console.log('🔄 Lấy danh sách sản phẩm từ product service...');
    const productsResponse = await getAllProducts();
    console.log('📦 Product service response:', productsResponse);
    
    const products = productsResponse.data || productsResponse;
    console.log('📦 Products data:', products);
    console.log('📦 Products count:', products.length);
    
    // Chuyển đổi dữ liệu sản phẩm thành format inventory
    const inventoryItems = products.map((product, index) => {
      console.log(`📦 Mapping product ${index + 1}:`, {
        id: product.id,
        name: product.name,
        stock: product.stock,
        available: product.available
      });
      
      return {
        id: product.id || index + 1,
        productId: product.id,
        productName: product.name || product.productName,
        totalQuantity: product.stock || 0,
        availableQuantity: product.stock || 0,
        soldQuantity: 0,
        lockedQuantity: 0,
        lowStockThreshold: 10,
        reorderPoint: 5,
        isAvailable: true,
        isActive: true,
        isLowStock: (product.stock || 0) <= 10,
        isOutOfStock: (product.stock || 0) === 0,
        needsReorder: (product.stock || 0) <= 5,
        lastSaleDate: new Date().toISOString(),
        createdAt: product.createdAt || new Date().toISOString(),
        updatedAt: product.updatedAt || new Date().toISOString()
      };
    });
    
    console.log('✅ Đã lấy được', inventoryItems.length, 'sản phẩm thật từ product service');
    console.log('📦 Mapped inventory items:', inventoryItems.slice(0, 3)); // Show first 3 items
    return { data: inventoryItems };
    
  } catch (error) {
    console.error('❌ Lỗi khi lấy sản phẩm từ product service:', error);
    console.log('❌ Không thể lấy dữ liệu sản phẩm');
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