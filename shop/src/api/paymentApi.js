// import axiosInstance from './axiosInstance';

// export const createPayment = async (paymentData) => {
//   try {
//     const response = await axiosInstance.post('/api/payments', paymentData);
//     return response.data;
//   } catch (error) {
//     console.error('Lỗi tạo thanh toán:', error);
//     throw error;
//   }
// };

// export const confirmPayment = async (orderId, status, gatewayResponse = null) => {
//   try {
//     const params = new URLSearchParams();
//     params.append('status', status);
//     if (gatewayResponse) {
//       params.append('gatewayResponse', gatewayResponse);
//     }
    
//     const response = await axiosInstance.post(`/api/payments/${orderId}/confirm?${params.toString()}`);
//     return response.data;
//   } catch (error) {
//     console.error('Lỗi xác nhận thanh toán:', error);
//     throw error;
//   }
// };

// export const getPaymentByOrderId = async (orderId) => {
//   try {
//     const response = await axiosInstance.get(`/api/payments/${orderId}`);
//     return response.data;
//   } catch (error) {
//     console.error('Lỗi lấy thông tin thanh toán:', error);
//     throw error;
//   }
// };

// export const cancelPayment = async (orderId) => {
//   try {
//     await axiosInstance.post(`/api/payments/${orderId}/cancel`);
//     return true;
//   } catch (error) {
//     console.error('Lỗi hủy thanh toán:', error);
//     throw error;
//   }
// }; 