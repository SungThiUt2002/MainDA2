//package com.stu.cart_service.client;
//
//
//import com.stu.cart_service.client.dto.UserResponse;
//import com.stu.cart_service.dto.response.ApiResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//
//
//
//@FeignClient(name = "account-service", url = "${app.account-service.url}")
//public interface AccountServiceClient {
//    @GetMapping("/users/myInfo")
//    ApiResponse<UserResponse> getMyInfo(@RequestHeader("Authorization") String token);
//}
