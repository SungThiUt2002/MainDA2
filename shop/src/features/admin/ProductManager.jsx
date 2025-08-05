import React, { useState, useEffect } from "react";
import {
  getAllProducts,
  deleteProduct, 
  updateProduct, 
  createProduct,
  getAllCategories,
  getAllBrands,
  setProductThumbnail,
  deleteProductImage,
  addProductImage,
  searchProducts
} from "../../api/productApi";
import { getAllInventoryItems, getInventoryByProductId } from "../../api/inventoryApi";
import AdvancedSearchFilter from "./AdvancedSearchFilter";
import "./ProductManager.css";

const ProductManager = () => {
  const [products, setProducts] = useState([]);
  const [inventoryData, setInventoryData] = useState({});
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inventoryError, setInventoryError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [showProductModal, setShowProductModal] = useState(false);
  const [showImageModal, setShowImageModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false);
  const [searchResults, setSearchResults] = useState(null);
  const [isSearching, setIsSearching] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedProductInventory, setSelectedProductInventory] = useState(null);
  const [editingProduct, setEditingProduct] = useState(null);
  const [productForm, setProductForm] = useState({
    name: "",
    description: "",
    price: "",
    categoryId: "",
    brandId: "",
    isActive: true,
  });
  const [error, setError] = useState("");

  useEffect(() => {
    fetchProducts();
    fetchInventoryData();
    fetchCategories();
    fetchBrands();
  }, []);

  const fetchProducts = async () => {
    try {
    setLoading(true);
      const res = await getAllProducts();
      console.log("📦 ProductManager - Products data:", res.data);
      console.log("📦 ProductManager - First product images:", res.data[0]?.images);
      setProducts(res.data || []);
    } catch (err) {
      console.error("Lỗi khi tải sản phẩm:", err);
      setError("Không thể tải danh sách sản phẩm");
    } finally {
      setLoading(false);
    }
  };

  const fetchInventoryData = async () => {
    try {
      const res = await getAllInventoryItems();
      const inventoryMap = {};
      if (res.data && Array.isArray(res.data)) {
        res.data.forEach(item => {
          inventoryMap[item.productId] = item;
        });
      }
      setInventoryData(inventoryMap);
    } catch (err) {
      console.error("Lỗi khi tải thông tin tồn kho:", err);
      setInventoryError("Không thể tải thông tin tồn kho");
      // Không set error state để tránh crash UI
      // Chỉ log lỗi và tiếp tục với dữ liệu rỗng
      setInventoryData({});
    } finally {
      // Inventory loading completed
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await getAllCategories();
      // Đảm bảo categories luôn là array
      const categoriesArray = Array.isArray(res) ? res : [];
      setCategories(categoriesArray);
    } catch (err) {
      console.error("Lỗi khi tải danh mục:", err);
      // Fallback data
      setCategories([
        { id: 1, name: "Tivi" },
        { id: 2, name: "Tủ lạnh" },
        { id: 3, name: "Máy giặt" },
      ]);
    }
  };

  const fetchBrands = async () => {
    try {
      const res = await getAllBrands();
      // Đảm bảo brands luôn là array
      const brandsArray = Array.isArray(res) ? res : [];
      setBrands(brandsArray);
    } catch (err) {
      console.error("Lỗi khi tải thương hiệu:", err);
      // Fallback data
      setBrands([
        { id: 1, name: "Sony" },
        { id: 2, name: "LG" },
        { id: 3, name: "Samsung" },
      ]);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  // 🔍 Advanced Search Handlers
  const handleAdvancedSearch = async (searchParams) => {
    setIsSearching(true);
    try {
      const response = await searchProducts(searchParams);
      setSearchResults(response.data);
      setShowAdvancedSearch(false);
    } catch (error) {
      console.error("Lỗi khi tìm kiếm:", error);
      alert("Có lỗi xảy ra khi tìm kiếm sản phẩm");
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearchReset = () => {
    setSearchResults(null);
    setIsSearching(false);
  };

  const handleCloseAdvancedSearch = () => {
    setShowAdvancedSearch(false);
  };

  // 🎯 Get current products to display
  const getCurrentProducts = () => {
    if (searchResults) {
      return searchResults.content || searchResults;
    }
    
    if (searchTerm) {
      return products.filter(product =>
        product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.description?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
    
    return products;
  };

  const currentProducts = getCurrentProducts();

  const handleViewProduct = async (product) => {
    setSelectedProduct(product);
    try {
      const inventoryRes = await getInventoryByProductId(product.id);
      if (inventoryRes.data) {
        setSelectedProductInventory(inventoryRes.data);
      } else {
        setSelectedProductInventory(null);
      }
    } catch (err) {
      console.error("Lỗi khi lấy thông tin tồn kho:", err);
      setSelectedProductInventory(null);
    }
    setShowDetailModal(true);
  };

  const handleEditProduct = (product) => {
    setEditingProduct(product);
    setProductForm({
      name: product.name,
      description: product.description,
      price: product.price,
      categoryId: product.categoryId,
      brandId: product.brandId,
      isActive: product.isActive,
    });
    setShowProductModal(true);
  };

  const handleDeleteProduct = async (id) => {
    if (window.confirm("Bạn có chắc muốn xóa sản phẩm này?")) {
      try {
        const token = localStorage.getItem('accessToken');
        await deleteProduct(id, token);
        fetchProducts();
      } catch (err) {
        console.error("Lỗi khi xóa sản phẩm:", err);
        setError("Không thể xóa sản phẩm");
      }
    }
  };

  // Image management functions
  const handleSetMainImage = async (imageIndex) => {
    try {
      const imageId = selectedProduct.images[imageIndex]?.id;
      if (!imageId) {
        alert('Không tìm thấy ID ảnh');
        return;
      }
      
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('Không tìm thấy token đăng nhập!');
        return;
      }
      
      await setProductThumbnail(selectedProduct.id, imageId, token);
      alert('Đã đặt ảnh làm ảnh chính thành công!');
      
      // Refresh data
      await fetchProducts();
      
      // Cập nhật selectedProduct với data mới
      const updatedProducts = await getAllProducts();
      const updatedProduct = updatedProducts.data.find(p => p.id === selectedProduct.id);
      if (updatedProduct) {
        setSelectedProduct(updatedProduct);
      }
      
      setShowImageModal(false);
    } catch (err) {
      console.error("Lỗi khi đặt ảnh chính:", err);
      alert("Không thể đặt ảnh chính");
    }
  };

  const handleDeleteImage = async (imageIndex) => {
    if (window.confirm("Bạn có chắc muốn xóa ảnh này?")) {
      try {
        const imageId = selectedProduct.images[imageIndex]?.id;
        if (!imageId) {
          alert('Không tìm thấy ID ảnh');
          return;
        }
        
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('Không tìm thấy token đăng nhập!');
          return;
        }
        
        await deleteProductImage(imageId, token);
        alert('Đã xóa ảnh thành công!');
        
        // Refresh data
        await fetchProducts();
        
        // Cập nhật selectedProduct với data mới
        const updatedProducts = await getAllProducts();
        const updatedProduct = updatedProducts.data.find(p => p.id === selectedProduct.id);
        if (updatedProduct) {
          setSelectedProduct(updatedProduct);
        }
        
        setShowImageModal(false);
      } catch (err) {
        console.error("Lỗi khi xóa ảnh:", err);
        alert("Không thể xóa ảnh");
      }
    }
  };

  const handleImageUpload = async (event) => {
    const files = event.target.files;
    if (files.length > 0) {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          alert('Không tìm thấy token đăng nhập!');
          return;
        }
        
        for (let i = 0; i < files.length; i++) {
          const file = files[i];
          const formData = new FormData();
          formData.append('file', file);
          formData.append('productId', selectedProduct.id);
          formData.append('isThumbnail', i === 0 ? 'true' : 'false'); // Ảnh đầu tiên làm thumbnail
          
          await addProductImage(formData, token);
        }
        
        alert('Upload ảnh thành công!');
        // Refresh data
        await fetchProducts();
        
        // Cập nhật selectedProduct với data mới
        const updatedProducts = await getAllProducts();
        const updatedProduct = updatedProducts.data.find(p => p.id === selectedProduct.id);
        if (updatedProduct) {
          setSelectedProduct(updatedProduct);
        }
        
        setShowImageModal(false);
      } catch (err) {
        console.error("Lỗi khi upload ảnh:", err);
        alert("Không thể upload ảnh");
      }
    }
  };

  const handleProductFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setProductForm({
      ...productForm,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleProductFormSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('accessToken');
      
      if (!token) {
        setError("Không tìm thấy token đăng nhập!");
        return;
      }
      
      // Convert data types
      const productData = {
        ...productForm,
        price: parseFloat(productForm.price) || 0,
        categoryId: productForm.categoryId ? parseInt(productForm.categoryId) : null,
        brandId: productForm.brandId ? parseInt(productForm.brandId) : null,
      };
      
      console.log('📤 Sending product data:', productData);
      console.log('📤 Token:', token);
      
      if (editingProduct) {
        await updateProduct(editingProduct.id, productData, token);
      } else {
        await createProduct(productData, token);
      }
      setShowProductModal(false);
      setEditingProduct(null);
      setProductForm({
        name: "",
        description: "",
        price: "",
        categoryId: "",
        brandId: "",
        isActive: true,
      });
      fetchProducts();
    } catch (err) {
      console.error("Lỗi khi lưu sản phẩm:", err);
      console.error("Error details:", err.response?.data);
      setError("Lưu thất bại!");
    }
  };

  const getStockStatus = (productId) => {
    const inventory = inventoryData[productId];
    if (!inventory) {
      return { 
        status: "unknown", 
        text: inventoryError ? "Lỗi kết nối" : "Chưa có thông tin", 
        color: inventoryError ? "#dc2626" : "#6b7280" 
      };
    }
    
    if (inventory.isOutOfStock) {
      return { status: "out-of-stock", text: "Hết hàng", color: "#dc2626" };
    } else if (inventory.isLowStock) {
      return { status: "low-stock", text: "Sắp hết", color: "#f59e0b" };
    } else if (inventory.needsReorder) {
      return { status: "needs-reorder", text: "Cần đặt hàng", color: "#d97706" };
    } else {
      return { status: "in-stock", text: "Còn hàng", color: "#059669" };
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  if (loading) {
    return <div className="product-manager">Đang tải...</div>;
  }

  return (
    <div className="product-manager">
      {/* Compact Header with Integrated Search */}
      <div className="product-header">
        <div className="header-left">
          <h2>📦 Quản lý sản phẩm</h2>
          <div className="search-controls">
            <input
              type="text"
              placeholder="🔍 Tìm kiếm sản phẩm..."
              className="search"
              value={searchTerm}
              onChange={handleSearch}
            />
            <button 
              className="btn-advanced-search"
              onClick={() => setShowAdvancedSearch(!showAdvancedSearch)}
            >
              🔍 Tìm kiếm nâng cao
            </button>
          </div>
        </div>
        <div className="header-actions">
          <button className="add-product-btn" onClick={() => {
            setEditingProduct(null);
            setProductForm({
              name: "",
              description: "",
              price: "",
              categoryId: "",
              brandId: "",
              isActive: true,
            });
            setShowProductModal(true);
          }}>
            + Thêm sản phẩm
          </button>
        </div>
      </div>

      {/* 🔍 Advanced Search Filter - Always visible when active */}
      {showAdvancedSearch && (
        <div className="search-section">
          <AdvancedSearchFilter
            onSearch={handleAdvancedSearch}
            onReset={handleSearchReset}
            onClose={handleCloseAdvancedSearch}
          />
        </div>
      )}

      {/* 📊 Search Results Info */}
      {isSearching && searchResults && (
        <div className="search-results-info">
          <span className="results-count">
            Tìm thấy {searchResults.totalElements || searchResults.length} sản phẩm
          </span>
          <button 
            className="btn-clear-search"
            onClick={handleSearchReset}
          >
            ✕ Xóa bộ lọc
          </button>
        </div>
      )}

      {error && <div className="error-message">{error}</div>}
      {inventoryError && (
        <div className="warning-message">
          ⚠️ {inventoryError} - Thông tin tồn kho có thể không chính xác
        </div>
      )}

        <table className="product-table">
          <thead>
            <tr>
            <th>Hình ảnh</th>
            <th>Tên sản phẩm</th>
              <th>Giá</th>
            <th>Tồn kho</th>
              <th>Trạng thái</th>
            <th>Hành động</th>
            </tr>
          </thead>
         <tbody>
          {currentProducts.map((product) => {
            const stockStatus = getStockStatus(product.id);
            const inventory = inventoryData[product.id];
            
            return (
              <tr key={product.id}>
                <td>
                  <div className="product-image-container">
                    {product.images && product.images.length > 0 ? (
                      <img
                        src={`http://localhost:9001/images/${product.images[0].url || product.images[0]}`}
                        alt={product.name}
                        className="thumb"
                        onError={(e) => {
                          e.target.style.display = 'none';
                          e.target.nextSibling.style.display = 'flex';
                        }}
                      />
                    ) : null}
                    <div className="no-thumb" style={{ 
                      display: (product.images && product.images.length > 0) ? 'none' : 'flex' 
                    }}>
                      <div className="placeholder-image">
                        <span>📦</span>
                        <small>No Image</small>
                      </div>
                    </div>
                  </div>
                </td>
                                 <td>
                  <div className="product-info" onClick={() => handleViewProduct(product)}>
                    <div className="product-name">{product.name}</div>
                    <div className="product-desc">
                      {product.description.substring(0, 50)}...
                    </div>
                  </div>
                </td>
                <td className="price">{formatCurrency(product.price)}</td>
                <td>
                  {inventory ? (
                    <div className="stock-display">
                      <div className="stock-main">
                        <span className="stock-number" style={{ color: stockStatus.color, fontWeight: 'bold' }}>
                          {inventory.availableQuantity}
                        </span>
                        <span className="stock-unit">cái</span>
                      </div>
                      <div className="stock-details">
                        <span className="stock-sold">Đã bán: {inventory.soldQuantity}</span>
                        {inventory.isLowStock && (
                          <span className="stock-warning">⚠️ Sắp hết</span>
                       )}
                     </div>
                    </div>
                  ) : (
                    <div className="stock-unknown">
                      <span>—</span>
                      <small>Chưa có dữ liệu</small>
                    </div>
                  )}
                 </td>
                <td>
                  <div className="status-badge" style={{ 
                    backgroundColor: stockStatus.color + '15',
                    color: stockStatus.color,
                    border: `1px solid ${stockStatus.color}`
                  }}>
                    <span className="status-icon">
                      {stockStatus.status === 'out-of-stock' && '❌'}
                      {stockStatus.status === 'low-stock' && '⚠️'}
                      {stockStatus.status === 'needs-reorder' && '🔄'}
                      {stockStatus.status === 'in-stock' && '✅'}
                      {stockStatus.status === 'unknown' && '❓'}
                    </span>
                    <span className="status-text">{stockStatus.text}</span>
                  </div>
                </td>
                <td>
                  <button
                    className="action-btn edit"
                    onClick={() => handleEditProduct(product)}
                  >
                    ✏️ Sửa
                    </button>
                  <button
                    className="action-btn images"
                    onClick={() => {
                      setSelectedProduct(product);
                      setShowImageModal(true);
                    }}
                  >
                    🖼️ Ảnh
                    </button>
                  <button
                    className="action-btn delete"
                    onClick={() => handleDeleteProduct(product.id)}
                  >
                    🗑️ Xóa
                    </button>
                </td>
                </tr>
            );
          })}
            </tbody>
        </table>

      {/* Product Detail Modal */}
      {showDetailModal && selectedProduct && (
        <div className="modal">
          <div className="modal-content product-detail-modal">
            <div className="modal-header">
              <h3>Chi tiết sản phẩm</h3>
              <button 
                className="close-btn"
                onClick={() => setShowDetailModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="product-detail-content">
              <div className="product-detail-images">
                <div className="image-gallery">
                  {selectedProduct.images && selectedProduct.images.length > 0 ? (
                    selectedProduct.images.map((image, index) => (
                      <div key={index} className={`image-item ${index === 0 ? 'thumbnail' : ''}`}>
                        <img
                          src={`http://localhost:9001/images/${typeof image === 'object' ? image.url : image}`}
                          alt={`${selectedProduct.name} ${index + 1}`}
                          className={index === 0 ? 'main-image' : 'gallery-image'}
                        />
                        {index === 0 && <span className="thumbnail-badge">Ảnh chính</span>}
                      </div>
                    ))
                  ) : (
                    <div className="no-images">Không có ảnh</div>
                  )}
                </div>
              </div>
              
              <div className="product-detail-info">
                <div className="detail-section">
                  <h4>Thông tin sản phẩm</h4>
                  <div className="detail-row">
                    <span className="label">Tên sản phẩm:</span>
                    <span className="value">{selectedProduct.name}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Giá:</span>
                    <span className="value price">{formatCurrency(selectedProduct.price)}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Trạng thái:</span>
                    <span className={`value badge ${selectedProduct.isActive ? 'active' : 'inactive'}`}>
                      {selectedProduct.isActive ? 'Hoạt động' : 'Không hoạt động'}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Mô tả:</span>
                    <div className="description-content">{selectedProduct.description}</div>
                  </div>
                </div>

                {selectedProductInventory && (
                  <div className="detail-section">
                    <h4>📦 Thông tin tồn kho</h4>
                    <div className="inventory-grid">
                      <div className="inventory-card primary">
                        <div className="inventory-icon">📦</div>
                        <div className="inventory-info">
                          <span className="inventory-label">Có sẵn để bán</span>
                          <span className="inventory-value available">
                            {selectedProductInventory.availableQuantity} cái
                          </span>
                        </div>
                      </div>
                      
                      <div className="inventory-card secondary">
                        <div className="inventory-icon">💰</div>
                        <div className="inventory-info">
                          <span className="inventory-label">Đã bán</span>
                          <span className="inventory-value sold">
                            {selectedProductInventory.soldQuantity} cái
                          </span>
                        </div>
                      </div>
                      
                      <div className="inventory-card secondary">
                        <div className="inventory-icon">🔒</div>
                        <div className="inventory-info">
                          <span className="inventory-label">Đang giữ</span>
                          <span className="inventory-value locked">
                            {selectedProductInventory.lockedQuantity} cái
                          </span>
                        </div>
                      </div>
                      
                      <div className="inventory-card secondary">
                        <div className="inventory-icon">📊</div>
                        <div className="inventory-info">
                          <span className="inventory-label">Tổng số lượng</span>
                          <span className="inventory-value total">
                            {selectedProductInventory.totalQuantity} cái
                          </span>
                        </div>
                      </div>
                    </div>
                    
                    <div className="inventory-alerts">
                      {selectedProductInventory.isLowStock && (
                        <div className="alert warning">
                          ⚠️ Sản phẩm sắp hết hàng (Dưới {selectedProductInventory.lowStockThreshold} cái)
                        </div>
                      )}
                      {selectedProductInventory.needsReorder && (
                        <div className="alert danger">
                          🔄 Cần đặt hàng lại (Dưới {selectedProductInventory.reorderPoint} cái)
                        </div>
                      )}
                      {selectedProductInventory.isOutOfStock && (
                        <div className="alert critical">
                          ❌ Hết hàng hoàn toàn
                        </div>
                      )}
                    </div>
                    
                    <div className="inventory-settings">
                      <div className="setting-item">
                        <span className="setting-label">Ngưỡng cảnh báo:</span>
                        <span className="setting-value">{selectedProductInventory.lowStockThreshold} cái</span>
                      </div>
                      <div className="setting-item">
                        <span className="setting-label">Điểm đặt hàng:</span>
                        <span className="setting-value">{selectedProductInventory.reorderPoint} cái</span>
                      </div>
                      <div className="setting-item">
                        <span className="setting-label">Trạng thái kho:</span>
                        <span className={`setting-value badge ${selectedProductInventory.isAvailable ? 'active' : 'inactive'}`}>
                          {selectedProductInventory.isAvailable ? '✅ Có bán' : '⏸️ Tạm ngưng'}
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                <div className="detail-actions">
                  <button 
                    className="action-btn edit"
                    onClick={() => {
                      setShowDetailModal(false);
                      handleEditProduct(selectedProduct);
                    }}
                  >
                    ✏️ Chỉnh sửa
                  </button>
                  <button 
                    className="action-btn images"
                    onClick={() => {
                      setShowDetailModal(false);
                      setShowImageModal(true);
                    }}
                  >
                    🖼️ Quản lý ảnh
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Product Form Modal */}
      {showProductModal && (
        <div className="modal-overlay">
          <div className="modal product-form-modal">
            <div className="modal-header">
              <div className="header-content">
                <div className="header-icon">
                  {editingProduct ? "✏️" : "➕"}
                </div>
                <div className="header-text">
                  <h3>{editingProduct ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới"}</h3>
                  <p>{editingProduct ? "Cập nhật thông tin sản phẩm" : "Tạo sản phẩm mới cho cửa hàng"}</p>
                </div>
              </div>
              <button 
                className="close-btn"
                onClick={() => setShowProductModal(false)}
              >
                ✕
              </button>
            </div>
            
            <form onSubmit={handleProductFormSubmit} className="product-form">
              <div className="form-sections">
                {/* Basic Information Section */}
                <div className="form-section">
                  <div className="section-header">
                    <span className="section-icon">📝</span>
                    <h4>Thông tin cơ bản</h4>
                  </div>
                  
                  <div className="form-group">
                    <label className="form-label">
                      <span className="label-icon">🏷️</span>
                      Tên sản phẩm
                      <span className="required">*</span>
                    </label>
                    <input
                      name="name"
                      value={productForm.name}
                      onChange={handleProductFormChange}
                      className="form-input"
                      placeholder="Nhập tên sản phẩm..."
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      <span className="label-icon">📄</span>
                      Mô tả sản phẩm
                      <span className="required">*</span>
                    </label>
                    <textarea
                      name="description"
                      value={productForm.description}
                      onChange={handleProductFormChange}
                      className="form-textarea"
                      rows="4"
                      placeholder="Mô tả chi tiết về sản phẩm..."
                      required
                    />
                  </div>
                </div>

                {/* Pricing & Category Section */}
                <div className="form-section">
                  <div className="section-header">
                    <span className="section-icon">💰</span>
                    <h4>Giá cả & Phân loại</h4>
                  </div>
                  
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">
                        <span className="label-icon">💵</span>
                        Giá sản phẩm
                        <span className="required">*</span>
                      </label>
                      <div className="price-input-wrapper">
                        <input
                          name="price"
                          type="number"
                          value={productForm.price}
                          onChange={handleProductFormChange}
                          className="form-input price-input"
                          placeholder="0"
                          min="0"
                          step="1000"
                          required
                        />
                        <span className="currency">VNĐ</span>
                      </div>
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">
                        <span className="label-icon">📂</span>
                        Danh mục
                        <span className="required">*</span>
                      </label>
                      <select
                        name="categoryId"
                        value={productForm.categoryId}
                        onChange={handleProductFormChange}
                        className="form-select"
                        required
                      >
                        <option value="">Chọn danh mục</option>
                        {categories.map(category => (
                          <option key={category.id} value={category.id}>
                            {category.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label className="form-label">
                        <span className="label-icon">🏢</span>
                        Thương hiệu
                        <span className="required">*</span>
                      </label>
                      <select
                        name="brandId"
                        value={productForm.brandId}
                        onChange={handleProductFormChange}
                        className="form-select"
                        required
                      >
                        <option value="">Chọn thương hiệu</option>
                        {brands.map(brand => (
                          <option key={brand.id} value={brand.id}>
                            {brand.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                {/* Status Section */}
                <div className="form-section">
                  <div className="section-header">
                    <span className="section-icon">⚙️</span>
                    <h4>Trạng thái</h4>
                  </div>
                  
                  <div className="form-group">
                    <label className="checkbox-label">
                      <input
                        name="isActive"
                        type="checkbox"
                        checked={productForm.isActive}
                        onChange={handleProductFormChange}
                        className="form-checkbox"
                      />
                      <span className="checkbox-custom"></span>
                      <span className="checkbox-text">
                        <span className="checkbox-icon">✅</span>
                        Sản phẩm đang hoạt động
                      </span>
                    </label>
                  </div>
                </div>
              </div>

              {error && (
                <div className="error-message">
                  <span className="error-icon">⚠️</span>
                  {error}
                </div>
              )}

              <div className="form-actions">
                <button type="button" 
                  className="btn-secondary"
                  onClick={() => {
                    setShowProductModal(false);
                    setEditingProduct(null);
                    setProductForm({
                      name: "",
                      description: "",
                      price: "",
                      categoryId: "",
                      brandId: "",
                      isActive: true,
                    });
                  }}
                >
                  <span className="btn-icon">❌</span>
                  Hủy bỏ
                </button>
                <button type="submit" className="btn-primary">
                  <span className="btn-icon">
                    {editingProduct ? "💾" : "➕"}
                  </span>
                  {editingProduct ? "Cập nhật sản phẩm" : "Thêm sản phẩm"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Image Manager Modal */}
      {showImageModal && selectedProduct && (
        <div className="modal">
          <div className="modal-content image-manager-modal">
            <div className="modal-header">
              <h3>Quản lý ảnh sản phẩm: {selectedProduct.name}</h3>
              <button 
                className="close-btn"
                onClick={() => setShowImageModal(false)}
              >
                ✕
              </button>
            </div>
            <div style={{ padding: '2rem' }}>
              <div className="image-manager-content">
                <div className="current-images">
                  <h4>Ảnh hiện tại ({selectedProduct.images?.length || 0})</h4>
                  <div className="image-grid">
                    {selectedProduct.images && selectedProduct.images.length > 0 ? (
                      selectedProduct.images.map((image, index) => (
                        <div key={index} className="image-item">
                          <img
                            src={`http://localhost:9001/images/${typeof image === 'object' ? image.url : image}`}
                            alt={`${selectedProduct.name} ${index + 1}`}
                            className="manage-image"
                          />
                          <div className="image-actions">
                            <button 
                              className="image-action-btn primary"
                              onClick={() => handleSetMainImage(index)}
                              disabled={index === 0}
                            >
                              {index === 0 ? '✅ Ảnh chính' : 'Đặt làm ảnh chính'}
                            </button>
                            <button 
                              className="image-action-btn danger"
                              onClick={() => handleDeleteImage(index)}
                            >
                              🗑️ Xóa
                            </button>
                          </div>
                          </div>
                      ))
                    ) : (
                      <div className="no-images">Chưa có ảnh nào</div>
                    )}
                  </div>
                    </div>

                <div className="upload-section">
                  <h4>Thêm ảnh mới</h4>
                  <div className="upload-area">
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      onChange={handleImageUpload}
                      className="file-input"
                    />
                    <div className="upload-placeholder">
                      <span>📁</span>
                      <p>Kéo thả ảnh vào đây hoặc click để chọn</p>
                      <small>Hỗ trợ: JPG, PNG, WebP (Tối đa 5MB/file)</small>
                    </div>
                  </div>
                 </div>
               </div>
             </div>
           </div>
         </div>
       )}
     </div>
   );
 };

export default ProductManager;