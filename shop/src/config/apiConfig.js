// API Configuration
const API_CONFIG = {
  // Base URL cho API calls
  BASE_URL: process.env.REACT_APP_API_BASE_URL || window.location.origin,
  
  // Base URL cho static images
  IMAGE_BASE_URL: process.env.REACT_APP_IMAGE_BASE_URL || "https://167.172.88.205/images/",
  
  // Product Service endpoints
  PRODUCT_SERVICE: {
    BASE_URL: process.env.REACT_APP_PRODUCT_SERVICE_URL || window.location.origin,
    IMAGE_BASE_URL: process.env.REACT_APP_PRODUCT_IMAGE_BASE_URL || "https://167.172.88.205/images/"
  }
};

export default API_CONFIG;
