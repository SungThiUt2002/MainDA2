import { createAxiosInstance } from './axiosInstance';

const axiosInstance = createAxiosInstance({
  baseURL: (typeof window !== 'undefined' ? window.location.origin : '')
});

// Lấy thông tin người dùng hiện tại
export const getMyInfo = async () => {
  try {
    const response = await axiosInstance.get('/users/myInfo');
    return response.data;
  } catch (error) {
    console.error('Lỗi lấy thông tin người dùng:', error);
    throw error;
  }
};

// Đổi mật khẩu
export const changePassword = async (changePasswordData) => {
  try {
    const response = await axiosInstance.post('/users/change-password', changePasswordData);
    return response.data;
  } catch (error) {
    console.error('Lỗi đổi mật khẩu:', error);
    throw error;
  }
};

// Cập nhật thông tin cá nhân
export const updateProfile = async (profileData) => {
  try {
    const response = await axiosInstance.put('/users/update', profileData);
    return response.data;
  } catch (error) {
    console.error('Lỗi cập nhật thông tin:', error);
    throw error;
  }
};

// Đăng xuất
export const logout = async () => {
  try {
    const response = await axiosInstance.post('/users/logout');
    return response.data;
  } catch (error) {
    console.error('Lỗi đăng xuất:', error);
    throw error;
  }
};
