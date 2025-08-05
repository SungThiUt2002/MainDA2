package com.stu.cart_service.controller;

import com.stu.cart_service.dto.request.*;
import com.stu.cart_service.dto.response.ApiResponse;
import com.stu.cart_service.dto.response.CartResponse;
import com.stu.cart_service.dto.response.CartItemResponse;
import com.stu.cart_service.mapper.CartMapper;
import com.stu.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final CartMapper cartMapper;


    // Lấy giỏ hàng ACTIVE theo userId
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<CartResponse> getActiveCartByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getActiveCartByUser(userId));
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/item")
    public ApiResponse<CartItemResponse> addCartItem(@Valid @RequestBody AddCartItemRequest request, @RequestHeader("Authorization") String token) {
        System.out.println("🛒 [CartController] addCartItem được gọi");
        System.out.println("📦 Request: " + request);
        System.out.println("🔑 Token: " + (token != null ? "Có token" : "Không có token"));

        // Cắt tiền tố "Bearer " nếu có và loại bỏ dấu cách thừa
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        
        CartItemResponse response = cartService.addCartItem(request, token);
        System.out.println("✅ [CartController] addCartItem thành công: " + response);
        
        return ApiResponse.<CartItemResponse>builder()
                .data(response)
                .code(200)
                .message("Thêm sản phẩm vào giỏ hàng thành công")
                .build();
    }


    // Tăng số lượng sản phẩm trong giỏ hàng lên 1
    @PutMapping("/increase-quantity/{cartItemId}")
    public ApiResponse<Void> increaseCartItemQuantity(@PathVariable Long cartItemId,  @RequestHeader("Authorization") String authorizationHeader) {

        cartService.increaseCartItemQuantity(cartItemId,authorizationHeader);
        return ApiResponse.<Void>builder()
                .message("Tăng số lượng thành công")
                .build();

    }
    // Tăng số lượng sản phẩm trong giỏ hàng lên 1
    @PutMapping("/decrease-quantity/{cartItemId}")
    public ApiResponse<Void> decreaseCartItemQuantity(@PathVariable Long cartItemId,  @RequestHeader("Authorization") String authorizationHeader) {

        cartService.decreaseCartItemQuantity(cartItemId,authorizationHeader);
        return ApiResponse.<Void>builder()
                .message("Giảm số lượng sản phẩm thành công")
                .build();

    }

    // Cập nhật sản phẩm trong giỏ hàng
//    @PutMapping("/item")
//    public ResponseEntity<CartItemResponse> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request, @RequestHeader("Authorization") String token) {
//        // Cắt tiền tố "Bearer " nếu có và loại bỏ dấu cách thừa
//        if (token != null && token.startsWith("Bearer ")) {
//            token = token.substring(7).trim();
//        }
//        return ResponseEntity.ok(cartService.updateCartItem(request, token));
//    }

    // Xóa sản phẩm khỏi giỏ hàng (chuyển trạng thái REMOVED)
    @DeleteMapping("/item/delete/{cartItemId}")
    public ApiResponse<Void> removeCartItem(@Valid @PathVariable Long cartItemId, @RequestHeader("Authorization") String authorizationHeader ) {
        cartService.removeCartItem(cartItemId, authorizationHeader);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm thành công")
                .code(200)
                .build();
    }

    // Lấy tất cả sản phẩm trong giỏ hàng
    @GetMapping("/{cartId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartItems(cartId));
    }

    // Lấy tất cả sản phẩm theo token
    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getAllCartItemByToken(@RequestHeader("Authorization") String authorizationHeader){
        return ResponseEntity.ok(cartService.getAllCartItemByToken(authorizationHeader));

    }

    // Checkout giỏ hàng
    @PostMapping("/checkout")
    public ResponseEntity<CartResponse> checkoutCart(@RequestHeader("Authorization") String authorizationHeader) {
        var cart =cartService.checkoutCart(authorizationHeader);
        var response = cartMapper.toResponse(cart);
        return ResponseEntity.ok(response);
    }

    //PUT /cart/item/123/select?selected=true
    @PutMapping("/item/{orderId}/select")
    public ResponseEntity<Void> selectItem(@PathVariable Long orderId, @RequestParam("selected") boolean selected,@RequestHeader("Authorization") String authorizationHeader) {
        cartService.selectCartItem(orderId, selected,authorizationHeader);
        return ResponseEntity.ok().build();
    }


}