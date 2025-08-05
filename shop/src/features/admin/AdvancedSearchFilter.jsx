import React, { useState, useEffect } from 'react';
import { 
  searchProducts, 
  getDistinctCategories, 
  getDistinctBrands, 
  getPriceRange 
} from '../../api/productApi';
import './AdvancedSearchFilter.css';

const AdvancedSearchFilter = ({ onSearch, onReset, onClose, isSearching }) => {
  // 🔍 Search state
  const [filters, setFilters] = useState({
    keyword: '',
    category: '',
    brand: '',
    minPrice: '',
    maxPrice: '',
    stockStatus: '',
    createdFrom: '',
    createdTo: '',
    updatedFrom: '',
    updatedTo: '',
    isActive: '',
    sortBy: 'name',
    sortOrder: 'asc',
    page: 0,
    size: 20
  });

  // 📊 Filter data state
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [priceRange, setPriceRange] = useState({ minPrice: 0, maxPrice: 0 });
  
  // 🔄 Loading states
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingFilters, setIsLoadingFilters] = useState(true);

  // 🎯 Load filter data on component mount
  useEffect(() => {
    loadFilterData();
  }, []);

  const loadFilterData = async () => {
    try {
      setIsLoadingFilters(true);
      
      // Load categories, brands, and price range in parallel
      const [categoriesRes, brandsRes, priceRangeRes] = await Promise.all([
        getDistinctCategories(),
        getDistinctBrands(),
        getPriceRange()
      ]);

      setCategories(categoriesRes.data || []);
      setBrands(brandsRes.data || []);
      setPriceRange(priceRangeRes.data || { minPrice: 0, maxPrice: 0 });
      
    } catch (error) {
      console.error('❌ Error loading filter data:', error);
    } finally {
      setIsLoadingFilters(false);
    }
  };

  // 🔄 Handle input changes
  const handleInputChange = (field, value) => {
    setFilters(prev => ({
      ...prev,
      [field]: value,
      page: 0 // Reset to first page when filters change
    }));
  };

  // 🔍 Handle search
  const handleSearch = async () => {
    try {
      setIsLoading(true);
      
      // Clean up empty values
      const cleanFilters = Object.fromEntries(
        Object.entries(filters).filter(([_, value]) => 
          value !== '' && value !== null && value !== undefined
        )
      );

      // Pass the search parameters to parent component instead of calling API directly
      onSearch(cleanFilters);
      
    } catch (error) {
      console.error('❌ Search error:', error);
      alert('Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại!');
    } finally {
      setIsLoading(false);
    }
  };

  // 🔄 Handle reset
  const handleReset = () => {
    setFilters({
      keyword: '',
      category: '',
      brand: '',
      minPrice: '',
      maxPrice: '',
      stockStatus: '',
      createdFrom: '',
      createdTo: '',
      updatedFrom: '',
      updatedTo: '',
      isActive: '',
      sortBy: 'name',
      sortOrder: 'asc',
      page: 0,
      size: 20
    });
    onReset();
  };

  // 🎯 Get active filters count
  const getActiveFiltersCount = () => {
    return Object.values(filters).filter(value => 
      value !== '' && value !== null && value !== undefined && 
      value !== 'name' && value !== 'asc' && value !== 0 && value !== 20
    ).length;
  };

  return (
    <div className="advanced-search-filter">
      {/* 🔍 Active Filters Badge */}
      <div className="active-filters">
        {getActiveFiltersCount() > 0 && (
          <span className="filter-badge">
            {getActiveFiltersCount()} bộ lọc đang hoạt động
          </span>
        )}
      </div>

      {/* 📊 Advanced Filters */}
      <div className="advanced-filters">
        <div className="filters-grid">
          {/* 🔍 Keyword Search */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">🔍</span>
              Từ khóa
            </label>
            <input
              type="text"
              placeholder="Tìm kiếm theo tên, mô tả..."
              value={filters.keyword}
              onChange={(e) => handleInputChange('keyword', e.target.value)}
              className="filter-input"
            />
          </div>

          {/* 🏷️ Category & Brand */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">🏷️</span>
              Danh mục
            </label>
            <select
              value={filters.category}
              onChange={(e) => handleInputChange('category', e.target.value)}
              className="filter-select"
            >
              <option value="">Tất cả danh mục</option>
              {categories.map(category => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">🏢</span>
              Thương hiệu
            </label>
            <select
              value={filters.brand}
              onChange={(e) => handleInputChange('brand', e.target.value)}
              className="filter-select"
            >
              <option value="">Tất cả thương hiệu</option>
              {brands.map(brand => (
                <option key={brand} value={brand}>
                  {brand}
                </option>
              ))}
            </select>
          </div>

          {/* 💰 Price Range */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">💰</span>
              Khoảng giá
            </label>
            <div className="price-range">
              <input
                type="number"
                placeholder="Từ"
                value={filters.minPrice}
                onChange={(e) => handleInputChange('minPrice', e.target.value)}
                className="price-input"
                min={priceRange.minPrice}
                max={priceRange.maxPrice}
              />
              <span className="price-separator">-</span>
              <input
                type="number"
                placeholder="Đến"
                value={filters.maxPrice}
                onChange={(e) => handleInputChange('maxPrice', e.target.value)}
                className="price-input"
                min={priceRange.minPrice}
                max={priceRange.maxPrice}
              />
              <span className="price-unit">VNĐ</span>
            </div>
          </div>

          {/* 🏷️ Status */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">📊</span>
              Trạng thái
            </label>
            <select
              value={filters.isActive}
              onChange={(e) => handleInputChange('isActive', e.target.value)}
              className="filter-select"
            >
              <option value="">Tất cả</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Không hoạt động</option>
            </select>
          </div>

          {/* 📅 Date Range */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">📅</span>
              Ngày tạo từ
            </label>
            <input
              type="date"
              value={filters.createdFrom}
              onChange={(e) => handleInputChange('createdFrom', e.target.value)}
              className="filter-input"
            />
          </div>

          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">📅</span>
              Ngày tạo đến
            </label>
            <input
              type="date"
              value={filters.createdTo}
              onChange={(e) => handleInputChange('createdTo', e.target.value)}
              className="filter-input"
            />
          </div>

          {/* 📊 Sorting */}
          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">📊</span>
              Sắp xếp theo
            </label>
            <select
              value={filters.sortBy}
              onChange={(e) => handleInputChange('sortBy', e.target.value)}
              className="filter-select"
            >
              <option value="name">Tên sản phẩm</option>
              <option value="price">Giá</option>
              <option value="createdAt">Ngày tạo</option>
              <option value="updatedAt">Ngày cập nhật</option>
            </select>
          </div>

          <div className="filter-group">
            <label className="filter-label">
              <span className="label-icon">🔄</span>
              Thứ tự
            </label>
            <select
              value={filters.sortOrder}
              onChange={(e) => handleInputChange('sortOrder', e.target.value)}
              className="filter-select"
            >
              <option value="asc">Tăng dần</option>
              <option value="desc">Giảm dần</option>
            </select>
          </div>
        </div>

        {/* 🎯 Filter Actions */}
        <div className="filter-actions">
          <button 
            className="btn-apply"
            onClick={handleSearch}
            disabled={isLoading || isLoadingFilters}
          >
            {isLoading ? '⏳ Đang tìm...' : '🔍 Tìm kiếm'}
          </button>
          <button 
            className="btn-reset"
            onClick={handleReset}
            disabled={isLoading}
          >
            🔄 Làm mới
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdvancedSearchFilter; 