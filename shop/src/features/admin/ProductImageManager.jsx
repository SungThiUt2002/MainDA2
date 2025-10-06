import React, { useState, useEffect, useRef } from "react";
import "./ProductImageManager.css";
import {
  getProductImages,
  addProductImage,
  deleteProductImage,
  setProductThumbnail,
  updateProductImage,
} from "../../api/productApi";
import API_CONFIG from "../../config/apiConfig";

const ProductImageManager = ({ productId, onImagesChange, onClose }) => {
  console.log("ProductImageManager rendered with productId:", productId); // Debug log
  
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const [showPreview, setShowPreview] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const fileInputRef = useRef(null);

  const token = localStorage.getItem("accessToken");

  useEffect(() => {
    if (productId) {
      fetchImages();
    }
  }, [productId]);

  const fetchImages = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await getProductImages(productId);
      setImages(res.data || []);
      if (onImagesChange) {
        onImagesChange(res.data || []);
      }
    } catch (err) {
      console.error("Error fetching images:", err);
      setError("Không thể tải danh sách ảnh!");
    }
    setLoading(false);
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleFileSelect = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFiles(e.target.files);
    }
  };

  const handleFiles = async (files) => {
    setUploading(true);
    setError("");
    setSuccessMessage("");
    
    try {
      let uploadedCount = 0;
      for (let file of files) {
        if (!file.type.startsWith('image/')) {
          setError("Chỉ chấp nhận file hình ảnh!");
          continue;
        }
        
        // Kiểm tra kích thước file (5MB)
        if (file.size > 5 * 1024 * 1024) {
          setError("File quá lớn! Tối đa 5MB.");
          continue;
        }
        
        // Tạo FormData để upload file
        const formData = new FormData();
        formData.append('file', file);
        formData.append('productId', productId);
        formData.append('isThumbnail', images.length === 0); // Ảnh đầu tiên là thumbnail
        
        await addProductImage(formData, token);
        uploadedCount++;
      }
      
      await fetchImages();
      
      if (uploadedCount > 0) {
        setSuccessMessage(`Đã upload thành công ${uploadedCount} ảnh!`);
        // Tự động ẩn thông báo sau 3 giây
        setTimeout(() => setSuccessMessage(""), 3000);
      }
    } catch (err) {
      console.error("Error uploading files:", err);
      setError("Upload ảnh thất bại! " + (err.response?.data?.message || err.message));
    }
    
    setUploading(false);
  };

  const handleDelete = async (imageId) => {
    if (!window.confirm("Bạn có chắc muốn xóa ảnh này?")) return;
    
    try {
      await deleteProductImage(imageId, token);
      await fetchImages();
      setSuccessMessage("Đã xóa ảnh thành công!");
      setTimeout(() => setSuccessMessage(""), 3000);
    } catch (err) {
      console.error("Error deleting image:", err);
      setError("Xóa ảnh thất bại!");
    }
  };

  const handleSetThumbnail = async (imageId) => {
    try {
      await setProductThumbnail(productId, imageId, token);
      await fetchImages();
      setSuccessMessage("Đã đặt ảnh đại diện thành công!");
      setTimeout(() => setSuccessMessage(""), 3000);
    } catch (err) {
      console.error("Error setting thumbnail:", err);
      setError("Đặt ảnh đại diện thất bại!");
    }
  };

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setShowPreview(true);
  };

  const getImageUrl = (image) => {
    if (/^https?:\/\//.test(image.url)) {
      return image.url;
    }
    return `${API_CONFIG.PRODUCT_SERVICE.IMAGE_BASE_URL}${image.url}`;
  };

  const reorderImages = async (fromIndex, toIndex) => {
    const newImages = [...images];
    const [movedImage] = newImages.splice(fromIndex, 1);
    newImages.splice(toIndex, 0, movedImage);
    
    setImages(newImages);
    
    // Cập nhật thứ tự trên backend
    try {
      for (let i = 0; i < newImages.length; i++) {
        await updateProductImage(newImages[i].id, {
          orderIndex: i
        }, token);
      }
    } catch (err) {
      console.error("Error reordering images:", err);
      setError("Cập nhật thứ tự ảnh thất bại!");
    }
  };

  const handleClose = () => {
    if (onClose) {
      onClose();
    }
  };

  if (!productId) {
    return <div className="error-message">Không có ID sản phẩm!</div>;
  }

  return (
    <div className="image-manager-container">
      <div className="image-manager-header">
        <h3>Quản lý ảnh sản phẩm</h3>
        <div className="image-stats">
          <span>{images.length} ảnh</span>
          {images.filter(img => img.isThumbnail).length > 0 && (
            <span className="thumbnail-badge">Có ảnh đại diện</span>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}
      {successMessage && <div className="success-message">{successMessage}</div>}

      {/* Upload Area */}
      <div 
        className={`upload-area ${dragActive ? 'drag-active' : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept="image/*"
          onChange={handleFileSelect}
          style={{ display: 'none' }}
        />
        <div className="upload-content">
          <div className="upload-icon">📷</div>
          <p>Kéo thả ảnh vào đây hoặc click để chọn</p>
          <p className="upload-hint">Hỗ trợ: JPG, PNG, WebP (Tối đa 5MB/file)</p>
        </div>
      </div>

      {uploading && (
        <div className="uploading-indicator">
          <div className="spinner"></div>
          <span>Đang upload ảnh...</span>
        </div>
      )}

      {/* Images Grid */}
      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <span>Đang tải ảnh...</span>
        </div>
      ) : (
        <div className="images-grid">
          {images.length === 0 ? (
            <div className="no-images">
              <p>Chưa có ảnh nào cho sản phẩm này</p>
              <p>Hãy upload ảnh đầu tiên!</p>
            </div>
          ) : (
            images.map((image, index) => (
              <div key={image.id} className="image-item">
                <div className="image-wrapper">
                  <img
                    src={getImageUrl(image)}
                    alt={`Product ${index + 1}`}
                    onClick={() => handleImageClick(image)}
                    className={`product-image ${image.isThumbnail ? 'thumbnail' : ''}`}
                    onError={(e) => {
                      e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMDAgNzBDMTE2LjU2OSA3MCAxMzAgODMuNDMxIDMwIDEwMEMxMzAgMTE2LjU2OSAxMTYuNTY5IDEzMCAxMDAgMTMwQzgzLjQzMSAxMzAgNzAgMTE2LjU2OSA3MCAxMEM3MCA4My40MzEgODMuNDMxIDcwIDEwMCA3MFoiIGZpbGw9IiM5Q0EzQUYiLz4KPC9zdmc+';
                    }}
                  />
                  {image.isThumbnail && (
                    <div className="thumbnail-badge">Đại diện</div>
                  )}
                  <div className="image-overlay">
                    <button 
                      className="overlay-btn preview-btn"
                      onClick={() => handleImageClick(image)}
                      title="Xem ảnh"
                    >
                      👁️
                    </button>
                    {!image.isThumbnail && (
                      <button 
                        className="overlay-btn thumbnail-btn"
                        onClick={() => handleSetThumbnail(image.id)}
                        title="Đặt làm ảnh đại diện"
                      >
                        ⭐
                      </button>
                    )}
                    <button 
                      className="overlay-btn delete-btn"
                      onClick={() => handleDelete(image.id)}
                      title="Xóa ảnh"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
                <div className="image-info">
                  <span className="image-name">{image.url.split('/').pop()}</span>
                  <span className="image-size">{image.fileSize || 'N/A'}</span>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Action Buttons */}
      <div className="image-manager-actions">
        <button className="btn-secondary" onClick={handleClose}>
          Đóng
        </button>
        <button className="btn-primary" onClick={handleClose}>
          Hoàn thành
        </button>
      </div>

      {/* Image Preview Modal */}
      {showPreview && selectedImage && (
        <div className="preview-modal" onClick={() => setShowPreview(false)}>
          <div className="preview-content" onClick={(e) => e.stopPropagation()}>
            <button className="close-btn" onClick={() => setShowPreview(false)}>
              ✕
            </button>
            <img 
              src={getImageUrl(selectedImage)} 
              alt="Preview" 
              className="preview-image"
              onError={(e) => {
                e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgdmlld0JveD0iMCAwIDQwMCA0MDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iNDAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0yMDAgMTQwQzIzMy4xMzggMTQwIDI2MCAxNjYuODYyIDI2MCAyMEMyNjAgMTY2Ljg2MiAyMzMuMTM4IDE5MCAyMDAgMTkwQzE2Ni44NjIgMTkwIDE0MCAxNjYuODYyIDE0MCAyMEMxNDAgMTY2Ljg2MiAxNjYuODYyIDE0MCAyMDAgMTQwWiIgZmlsbD0iIzlDQTNBRiIvPgo8L3N2Zz4=';
              }}
            />
            <div className="preview-info">
              <h4>Thông tin ảnh</h4>
              <p><strong>Tên file:</strong> {selectedImage.url.split('/').pop()}</p>
              <p><strong>Kích thước:</strong> {selectedImage.fileSize || 'N/A'}</p>
              <p><strong>Loại:</strong> {selectedImage.isThumbnail ? 'Ảnh đại diện' : 'Ảnh phụ'}</p>
              <p><strong>Ngày tạo:</strong> {selectedImage.createdAt ? new Date(selectedImage.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductImageManager;

