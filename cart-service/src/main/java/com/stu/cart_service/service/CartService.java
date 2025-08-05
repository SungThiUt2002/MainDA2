package com.stu.cart_service.service;

import com.stu.cart_service.client.InventoryServiceClient;
import com.stu.cart_service.client.ProductServiceClient;


import com.stu.cart_service.client.dto.ProductInfo;
import com.stu.cart_service.dto.request.*;
import com.stu.cart_service.dto.response.CartResponse;
import com.stu.cart_service.dto.response.CartItemResponse;
import com.stu.cart_service.entity.Cart;
import com.stu.cart_service.entity.CartItem;
import com.stu.cart_service.entity.TokenBlacklistCache;
import com.stu.cart_service.enums.CartStatus;
import com.stu.cart_service.enums.CartItemStatus;
import com.stu.cart_service.event.producer.CartCheckedOutEventPublisher;
import com.stu.cart_service.exception.AppException;
import com.stu.cart_service.exception.ErrorCode;
import com.stu.cart_service.mapper.CartMapper;
import com.stu.cart_service.mapper.CartItemMapper;
import com.stu.cart_service.repository.CartRepository;
import com.stu.cart_service.repository.CartItemRepository;


import com.stu.cart_service.security.JwtUtil;
import com.stu.common_dto.event.CartEvent.CartCheckedOutEvent;
import com.stu.common_dto.event.CartEvent.CartItemDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductServiceClient productServiceClient;
    private  final InventoryServiceClient inventoryServiceClient;
    private  final JwtUtil jwtUtil;
    private final TokenBlacklistCache tokenBlacklistCache;
    private final CartCheckedOutEventPublisher  cartCheckedOutEventPublisher;


    // 1. Tạo giỏ hàng mới (internal - account service)
    // Khi người dùng đăng ký tài khoản --> gọi tới hàm này để tự động tạo giỏ hàng tương ứng
    public CartResponse createCart(CartRequest request) {
        // Kiểm tra user đã có cart ACTIVE chưa
        Optional<Cart> existing = cartRepository.findByUserIdAndStatus(request.getUserId(), CartStatus.ACTIVE);
        if (existing.isPresent()) {
            throw new AppException(ErrorCode.CART_ALREADY_EXISTS, "User đã có giỏ hàng ACTIVE");
        }

        Cart cart = cartMapper.toEntity(request);
        cart.setStatus(CartStatus.ACTIVE);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toResponse(saved);
    }


    // 2. Xóa giỏ hàng (internal - account service)
    // Nhận event xóa tài khoản từ product service --> xóa giỏ hàng tương ứng
    @Transactional
    public void deleteCartByUserId(Long userId) {
        Cart cart= cartRepository.findByUserId(userId)
                .orElseThrow(()-> new AppException(ErrorCode.CART_NOT_FOUND));

        log.info("Deleting all carts for userId: {}", userId);
        cartRepository.deleteByUserId(userId);
    }


    // 3. Vô hiệu hóa giỏ hàng của user khi user bị disable
    @Transactional
    public void disableCartByUserId(Long userId) {
        log.info("Disabling all carts for userId: {}", userId);
        cartRepository.updateStatusByUserId(userId, CartStatus.INACTIVE);
    }


    // ==========================================================================//

     // Lấy giỏ hàng ACTIVE theo userId (admin)
    public CartResponse getActiveCartByUser(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND, "Không tìm thấy giỏ hàng ACTIVE cho user"));
        return cartMapper.toResponse(cart);
    }

    //=============================================================================//

    // 4. Thêm sản phẩm vào giỏ hàng (USER)
    @Transactional
    public CartItemResponse addCartItem(AddCartItemRequest request, String token) {
        System.out.println("🛒 [CartService] addCartItem được gọi");
        System.out.println("📦 Request: " + request);
        System.out.println("🔑 Token: " + (token != null ? "Có token" : "Không có token"));

        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }
        log.info("Token nhận được: '{}'", token);


        // 1. Tìm CartId theo userID(lấy trong token)
        Long userIdFromToken = jwtUtil.extractUserId(token);
        Cart cart = cartRepository.findByUserId(userIdFromToken)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

//        if(!cart.getUserId().equals(userIdFromToken))
//        {
//            log.info("User validated: {} (id: {})", userIdFromToken);
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }

        // 2. Kiểm tra trạng thái cart phải là ACTIVE
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new AppException(ErrorCode.CART_NOT_ACTIVE, "Giỏ hàng không ở trạng thái hoạt động");
        }

        // 3. Validate product tồn tại và lấy thông tin
        ProductInfo productInfo = null;
        try {
            productInfo = productServiceClient.getProductForClient(request.getProductId());
            if (productInfo == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_AVAILABLE, "Sản phẩm không khả dụng");
            }
            log.info("Product validated: {}", productInfo.getProductName());
        } catch (Exception e) {
            log.error("Error calling product service: {}", e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        // 3. kiểm tra số lượng sản phẩm tồn kho so với giỏ hàng
        int available = inventoryServiceClient.getAvailableQuantity(request.getProductId());
        if (available < request.getQuantity()) {
            throw new AppException(ErrorCode.OUT_OF_STOCK, "Sản phẩm không đủ tồn kho");
        }

        // 4. Nếu đã có sản phẩm trong giỏ thì tăng số lượng
        Optional<CartItem> existing = cartItemRepository.findByCartAndProductId(cart, request.getProductId());
        if (existing.isPresent()) {
            CartItem existingItem = existing.get();
            int addQuantity = (request.getQuantity() == null || request.getQuantity() == 1) ? 1 : request.getQuantity();
            existingItem.setQuantity(existingItem.getQuantity() + addQuantity);
            CartItem updatedItem = cartItemRepository.save(existingItem);
            return cartItemMapper.toResponse(updatedItem);
        }

        // 5. Tạo cart item
        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(productInfo.getProductId())
                .productName(productInfo.getProductName())
                .quantity(request.getQuantity())
                .price(productInfo.getPrice())
                .status(CartItemStatus.ACTIVE)
                .build();
        CartItem saved = cartItemRepository.save(item);
        return cartItemMapper.toResponse(saved);
    }

    /**
     * 5. Tăng số lượng sản phẩm trong giỏ hàng theo CartItemId (by USER)
     * Mặc định + 1, mỗi lần thêm
     */
    @Transactional
    public void increaseCartItemQuantity(Long cartItemId, String token) {

        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        //1. Tìm sản phẩm
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(()->new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 2. Kiểm tra trạng thái cảu sản phẩm trong giỏ hàng
        if (item.getStatus() != CartItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_ACTIVE);
        }

        //2. Xác thực người dùng (xem có đúng người sở hữu giỏ hàng không)
        Long userIdFromToken = jwtUtil.extractUserId(token);
        if(!item.getCart().getUserId().equals(userIdFromToken))
        {
            log.info("User validated: {} (id: {})", userIdFromToken);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. tăng lên 1
        int newQuantity = item.getQuantity() + 1;

        // 4. Lưu lại số lượng mới
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

    }

    /**
     * 6. Giảm số lượng sản phẩm trong giỏ hàng theo CartItemId
     * Mặc định mỗi lần gọi là giảm đi 1
     */
    @Transactional
    public void decreaseCartItemQuantity(Long cartItemId, String token) {
        //0.Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        //1. Tìm sản phẩm xem đã có trong giỏ hàng hay chưa
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(()->new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 2. Kiểm tra trạng thái cảu sản phẩm trong giỏ hàng
        if (item.getStatus() != CartItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_ACTIVE);
        }
        // 3. Xác thực người dùng (xem có đúng người sở hữu giỏ hàng không)
        Long userIdFromToken = jwtUtil.extractUserId(token);
        if(!item.getCart().getUserId().equals(userIdFromToken))
        {
            log.info("User validated: {} (id: {})", userIdFromToken);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 4. Giảm số lượng đi một
        int newQuantity = item.getQuantity() - 1;

        // 5. Lưu lại số lượng sau khi giảm
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    /**
     * 7. Xóa sản phẩm khỏi giỏ hàng (chuyển trạng thái REMOVED)
     */
    @Transactional
    public void removeCartItem(Long cartItemId, String token) {
        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        // 1. Tìm sản phảm xem đã có trong giỏ hàng hay chưa
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

//        // 2. Kiểm tra trạng thái cảu sản phẩm trong giỏ hàng
//        if (item.getStatus() != CartItemStatus.ACTIVE) {
//            throw new AppException(ErrorCode.CART_ITEM_NOT_ACTIVE);
//        }

        // 3. Xác thực người dùng (xem có đúng người sở hữu giỏ hàng không)
        Long userIdFromToken = jwtUtil.extractUserId(token);
        if(!item.getCart().getUserId().equals(userIdFromToken))
        {
            log.info("User validated: {} (id: {})", userIdFromToken);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xóa sản phẩm khỏi giỏ hàng
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * 8.Lấy tất cả sản phẩm trong giỏ hàng theo id
     */
    public List<CartItemResponse> getCartItems(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> items = cartItemRepository.findByCart(cart);
        return cartItemMapper.toResponseList(items);
    }

    //9.Lấy tất cả sản phẩm trng giỏ hàng theo userId từ token(bởi vì user không hề biết id của mình)
    public List<CartItemResponse> getAllCartItemByToken(String token){

        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        // Tìm CartId theo userID(lấy trong token)
        Long userIdFromToken = jwtUtil.extractUserId(token);
        Cart cart = cartRepository.findByUserId(userIdFromToken)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        log.info("userId:", userIdFromToken);
        log.info("CartId:", cart);

        List<CartItem> items = cartItemRepository.findByCart(cart);

        return cartItemMapper.toResponseList(items);
    }

    /**
     * 10. Checkout giỏ hàng (chuyển trạng thái CHECKED_OUT--> chuẩn bị gửi sang order-service)
     * KHi người đung ấn (MUA HÀNG) trong giỏ hảng thì sẽ gọi đến hàm này
     */
    @Transactional
    public Cart checkoutCart(String token) {

        // 0. Xác thực token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }

        // Lấy giỏ hàng theo userId từ token
        Long userIdFromToken = jwtUtil.extractUserId(token);
        Cart cart = cartRepository.findByUserId(userIdFromToken)
                .orElseThrow(()-> new  AppException(ErrorCode.CART_NOT_FOUND));

        // kiêm tra trạng thái của giỏ hàng
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new AppException(ErrorCode.CART_STATUS_INVALID, "Chỉ có thể checkout giỏ hàng ACTIVE");
        }

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(item -> item.getStatus() == CartItemStatus.CHECKED_OUT)
                .toList();

        if (selectedItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "Không có sản phẩm nào được chọn để đặt hàng");
        }

        // gửi event cho order service để tiến hành tạo đơn hàng
        List<CartItemDTO> itemDTOs = selectedItems.stream()
                .map(item -> new CartItemDTO(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        CartCheckedOutEvent event = new CartCheckedOutEvent();
        event.setUserId(userIdFromToken);
        event.setItems(itemDTOs);
        cartCheckedOutEventPublisher.sendCartCheckedOutEvent(event);

        // Xóa các item đã đặt hàng trong giỏ hàng
        selectedItems.forEach(cartItemRepository::delete);

//        // Cập nhật danh sách item trong cart nếu trả về
        cart.getItems().removeIf(item -> item.getStatus() == CartItemStatus.CHECKED_OUT);

        return cart;
    }

    //11. Đánh dấu trạng thái của sản phẩm
    // chuẩn bị cho việc đặt hàng

    public void selectCartItem( Long productId, boolean selected, String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token hết hạn");
        }

        // kiểm tra xem token đã bị thu hồi hay chưa(logout, đổi mật khẩu)
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistCache.contains(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi");
        }
        // Kiểm tra quyền sở hữu
        Long currentUserId = jwtUtil.extractUserId(token);

        Cart cart = cartRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

         CartItem item = cartItemRepository.findByCartAndProductId(cart,productId)
                 .orElseThrow(()-> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        item.setStatus(selected ? CartItemStatus.CHECKED_OUT : CartItemStatus.ACTIVE);
        cartItemRepository.save(item);
    }
}

// Thiếu
// Hàm tăng giảm số lượng, đảm bảo rằng khi số lượng sản phẩm =0 ==> thì xóa sản phẩm ra khỏi giỏ hàng