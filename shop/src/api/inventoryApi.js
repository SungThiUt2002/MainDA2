import { createAxiosInstance } from './axiosInstance';

const inventoryApi = createAxiosInstance({
  baseURL: 'http://167.172.88.205/api/v1/inventory-items',
});


const mockInventoryData = [
  // Sản phẩm còn hàng (130 sản phẩm)
  ...Array.from({ length: 130 }, (_, i) => ({
    id: i + 1,
    productId: i + 1,
    productName: `Sản phẩm ${i + 1}`,
    totalQuantity: 50,
    availableQuantity: 35,
    soldQuantity: 15,
    lockedQuantity: 0,
    lowStockThreshold: 10,
    reorderPoint: 5,
    isAvailable: true,
    isActive: true,
    isLowStock: false,
    isOutOfStock: false,
    needsReorder: false,
    lastSaleDate: "2024-01-15T10:30:00",
    createdAt: "2024-01-01T00:00:00",
    updatedAt: "2024-01-15T10:30:00"
  })),
  
  // Sản phẩm sắp hết hàng (8 sản phẩm)
  ...Array.from({ length: 8 }, (_, i) => ({
    id: 131 + i,
    productId: 131 + i,
    productName: `Sản phẩm sắp hết ${i + 1}`,
    totalQuantity: 30,
    availableQuantity: 8,
    soldQuantity: 22,
    lockedQuantity: 0,
    lowStockThreshold: 10,
    reorderPoint: 5,
    isAvailable: true,
    isActive: true,
    isLowStock: true,
    isOutOfStock: false,
    needsReorder: true,
    lastSaleDate: "2024-01-14T15:45:00",
    createdAt: "2024-01-01T00:00:00",
    updatedAt: "2024-01-14T15:45:00"
  })),
  
  // Sản phẩm hết hàng (12 sản phẩm)
  ...Array.from({ length: 12 }, (_, i) => ({
    id: 139 + i,
    productId: 139 + i,
    productName: `Sản phẩm hết hàng ${i + 1}`,
    totalQuantity: 20,
    availableQuantity: 0,
    soldQuantity: 20,
    lockedQuantity: 0,
    lowStockThreshold: 10,
    reorderPoint: 5,
    isAvailable: false,
    isActive: true,
    isLowStock: false,
    isOutOfStock: true,
    needsReorder: true,
    lastSaleDate: "2024-01-13T09:20:00",
    createdAt: "2024-01-01T00:00:00",
    updatedAt: "2024-01-13T09:20:00"
  }))
];

// Lấy thông tin tồn kho của một sản phẩm theo productId
export const getInventoryByProductId = async (productId) => {
  try {
    const response = await inventoryApi.get(`/product/${productId}`);
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy thông tin tồn kho:', error);
    // Fallback to mock data
    const mockItem = mockInventoryData.find(item => item.productId === productId);
    if (mockItem) {
      return { data: mockItem };
    }
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
    // Fallback to mock data
    const mockItem = mockInventoryData.find(item => item.productId === productId);
    if (mockItem) {
      return { data: mockItem.availableQuantity };
    }
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
    // Fallback to mock data
    const mockItem = mockInventoryData.find(item => item.productId === productId);
    if (mockItem) {
      return { data: mockItem.soldQuantity };
    }
    throw error;
  }
};

// Lấy tất cả thông tin tồn kho
export const getAllInventoryItems = async () => {
  try {
    const response = await inventoryApi.get('/all');
    console.log('Inventory API response:', response);
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tồn kho:', error);
    console.error('Error details:', error.response?.data);
    throw error; // Không fallback về mock data nữa
  }
};

// Lấy danh sách sản phẩm có sẵn
export const getAvailableInventoryItems = async () => {
  try {
    const response = await inventoryApi.get('/available');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm có sẵn:', error);
    // Fallback to mock data
    const availableItems = mockInventoryData.filter(item => item.isAvailable && item.availableQuantity > 0);
    return { data: availableItems };
  }
};

// Lấy danh sách sản phẩm sắp hết hàng
export const getLowStockItems = async () => {
  try {
    const response = await inventoryApi.get('/low-stock');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm sắp hết hàng:', error);
    // Fallback to mock data
    const lowStockItems = mockInventoryData.filter(item => item.isLowStock);
    return { data: lowStockItems };
  }
};

// Lấy danh sách sản phẩm cần đặt hàng lại
export const getItemsNeedingReorder = async () => {
  try {
    const response = await inventoryApi.get('/needing-reorder');
    return response;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm cần đặt hàng:', error);
    // Fallback to mock data
    const reorderItems = mockInventoryData.filter(item => item.needsReorder);
    return { data: reorderItems };
  }
};

// Nhập kho cho sản phẩm
export const importStock = async (productId, stockData, token) => {
  try {
    console.log('Importing stock with data:', { productId, stockData, token: token ? 'Bearer ***' : 'No token' });
    
    const response = await inventoryApi.post(`/${productId}`, stockData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    console.log('Import stock response:', response);
    return response;
  } catch (error) {
    console.error('Lỗi khi nhập kho:', error);
    console.error('Error response:', error.response?.data);
    console.error('Error status:', error.response?.status);
    throw error;
  }
}; 