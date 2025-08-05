import React, { useState, useEffect } from 'react';
import { 
  getDistinctCategories, 
  getDistinctBrands, 
  getPriceRange 
} from '../../api/productApi';
import './HomePageSearchFilter.css';

const HomePageSearchFilter = ({ onSearch, onReset, isSearching }) => {
  // 🔍 Search state - Chỉ giữ lại các mục cần thiết cho HomePage
  const [filters, setFilters] = useState({
    keyword: '',
    category: '',
    brand: '',
    minPrice: '',
    maxPrice: '',
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
  
  // 🎯 UI state
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);

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

      // Pass the search parameters to parent component
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
    <div className="homepage-search-filter">
      {/* 🔍 Active Filters Badge */}
      <div className="active-filters">
        {getActiveFiltersCount() > 0 && (
          <span className="filter-badge">
            {getActiveFiltersCount()} bộ lọc đang hoạt động
          </span>
        )}
      </div>

      {/* 📊 Search Filters - Layout cải thiện */}
      <div className="search-filters">
        {/* 🎯 Primary Filters - Luôn hiển thị */}
        <div className="primary-filters">
          <div className="filters-grid-single-row">
            {/* 🏷️ Category - Đặt ở đầu */}
            <div className="filter-group category-group">
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

            {/* 🔍 Keyword Search - Ở giữa */}
            <div className="filter-group keyword-group">
              <label className="filter-label">
                <span className="label-icon">🔍</span>
                Tìm kiếm
              </label>
              <input
                type="text"
                placeholder="Nhập tên sản phẩm, thương hiệu..."
                value={filters.keyword}
                onChange={(e) => handleInputChange('keyword', e.target.value)}
                className="filter-input keyword-input"
              />
            </div>

            {/* 🔍 Search Button */}
            <div className="filter-actions-primary">
              <button 
                className="btn-apply"
                onClick={handleSearch}
                disabled={isLoading || isLoadingFilters}
              >
                {isLoading ? '⏳ Đang tìm...' : '🔍 Tìm kiếm'}
              </button>
            </div>

            {/* 🔄 Reset Button */}
            <div className="filter-actions-primary">
              <button 
                className="btn-reset"
                onClick={handleReset}
                disabled={isLoading}
              >
                🔄 Làm mới
              </button>
            </div>

            {/* 🎯 Advanced Filters Button - Ở cuối cùng bên phải */}
            <div className="advanced-filters-button">
              <button 
                className="toggle-advanced-btn"
                onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
              >
                <span className="toggle-icon">
                  {showAdvancedFilters ? '▼' : '▶'}
                </span>
                Bộ lọc nâng cao
                <span className="toggle-count">
                  ({getActiveFiltersCount()})
                </span>
              </button>
            </div>
          </div>
        </div>

        {/* 🎯 Advanced Filters Content - Hiển thị bên dưới khi mở */}
        {showAdvancedFilters && (
          <div className="advanced-filters-content">
            <div className="filters-grid">
              {/* 🏢 Brand */}
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

              {/* 📊 Sorting */}
              <div className="filter-group">
                <label className="filter-label">
                  <span className="label-icon">📊</span>
                  Sắp xếp
                </label>
                <select
                  value={filters.sortBy}
                  onChange={(e) => handleInputChange('sortBy', e.target.value)}
                  className="filter-select"
                >
                  <option value="name">Tên sản phẩm</option>
                  <option value="price">Giá tăng dần</option>
                  <option value="price">Giá giảm dần</option>
                  <option value="createdAt">Mới nhất</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomePageSearchFilter; 