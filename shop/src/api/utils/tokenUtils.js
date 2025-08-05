// // Giải mã token và kiểm tra hết hạn
// export function isTokenExpired(token, bufferSeconds = 60) {
//   if (!token) return true;

//   try {
//     const payload = JSON.parse(atob(token.split('.')[1]));
//     const exp = payload.exp * 1000;
//     return Date.now() >= (exp - bufferSeconds * 1000);
//   } catch (err) {
//     console.error("Invalid token format", err);
//     return true;
//   }
// }

// /**
//  * Hàm kiểm tra JWT token đã hết hạn hay chưa.
//  * @param {string} token - JWT access token (dạng: header.payload.signature).
//  * @param {number} bufferSeconds - Số giây đệm để kiểm tra token sắp hết hạn (mặc định: 60 giây).
//  * @returns {boolean} - true nếu token đã hết hạn hoặc không hợp lệ, false nếu còn hiệu lực.
//  */
export function isTokenExpired(token, bufferSeconds = 60) {
  // Nếu không có token => xem như hết hạn
  if (!token) return true;

  try {
    // Giải mã phần payload của token (phần ở giữa) bằng hàm atob (Base64 decode)
    const payloadBase64 = token.split('.')[1]; // phần payload
    const payloadJson = atob(payloadBase64); // giải mã base64 thành chuỗi JSON
    const payload = JSON.parse(payloadJson);  // parse thành object

    // Thời gian hết hạn token (trong giây, chuyển sang milliseconds)
    const expirationTime = payload.exp * 1000;

    // Kiểm tra thời gian hiện tại >= thời gian hết hạn - buffer => xem như token đã (hoặc sắp) hết hạn
    const currentTime = Date.now(); // thời gian hiện tại (ms)
    return currentTime >= (expirationTime - bufferSeconds * 1000);
  } catch (err) {
    // Nếu token sai định dạng hoặc decode thất bại => xem như hết hạn
    console.error("❌ Token không hợp lệ:", err);
    return true;
  }
}
