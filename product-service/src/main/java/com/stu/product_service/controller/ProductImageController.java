package com.stu.product_service.controller;

import com.stu.product_service.dto.request.CreateProductImageRequest;
import com.stu.product_service.dto.request.UpdateProductImageRequest;
import com.stu.product_service.dto.response.ApiResponse;
import com.stu.product_service.dto.response.ProductImageResponse;
import com.stu.product_service.service.ProductImageService;
import com.stu.product_service.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductImageController {
    private final ProductImageService productImageService;
    private final ProductImageMapper productImageMapper;
    private final Path imagesPath = Paths.get("/app/images");

    // ==================== API QUẢN LÝ ẢNH SẢN PHẨM ====================
    
    // Lấy tất cả ảnh chung của sản phẩm (không có ảnh của variants)
    @GetMapping("/api/v1/product-images/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
        var images = productImageService.getProductImages(productId);
        var responses = productImageMapper.toResponseList(images);
        return ResponseEntity.ok(responses);
    }

    // Lấy ảnh thumbnail của sản phẩm (lấy ảnh đại diện của sản phẩm)
    @GetMapping("/api/v1/product-images/product/{productId}/thumbnail")
    public ResponseEntity<ProductImageResponse> getProductThumbnail(@PathVariable Long productId) {
        var image = productImageService.getProductThumbnail(productId);
        
        if (image == null) {
            // Trả về response rỗng khi không có thumbnail
            return ResponseEntity.ok(ProductImageResponse.builder()
                    .id(null)
                    .url(null)
                    .isThumbnail(false)
                    .build());
        }
        
        var response = productImageMapper.toResponse(image);
        return ResponseEntity.ok(response);
    }

//    // Tạo ảnh mới (với URL)
//    @PostMapping("/api/v1/product-images")
//    public ResponseEntity<ProductImageResponse> createImage(@RequestBody CreateProductImageRequest request) {
//        var image = productImageService.createImage(request);
//        var response = productImageMapper.toResponse(image);
//        return ResponseEntity.ok(response);
//    }

    // ✅ Upload file thực tế
    @PostMapping(value = "/api/v1/product-images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId,
            @RequestParam(value = "isThumbnail", defaultValue = "false") Boolean isThumbnail) {
        var image = productImageService.uploadImage(file, productId, isThumbnail);
        var response = productImageMapper.toResponse(image);
        return ResponseEntity.ok(response);
    }

    // Cập nhật ảnh
    @PutMapping("/api/v1/product-images/update/{imageId}")
    public ResponseEntity<ProductImageResponse> updateImage(
            @PathVariable Long imageId,
            @RequestBody UpdateProductImageRequest request) {
        var image = productImageService.updateImage(imageId, request);
        var response = productImageMapper.toResponse(image);
        return ResponseEntity.ok(response);
    }

    // Xóa ảnh
    @DeleteMapping("/api/v1/product-images/delete/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    // Lấy chi tiết ảnh
    @GetMapping("/api/v1/product-images/{imageId}")
    public ResponseEntity<ProductImageResponse> getImage(@PathVariable Long imageId) {
        var image = productImageService.getImage(imageId);
        var response = productImageMapper.toResponse(image);
        return ResponseEntity.ok(response);
    }

    // Đếm số ảnh của sản phẩm
    @GetMapping("/api/v1/product-images/product/{productId}/count")
    public ResponseEntity<Long> countProductImages(@PathVariable Long productId) {
        long count = productImageService.countProductImages(productId);
        return ResponseEntity.ok(count);
    }

    // ✅ Đặt ảnh làm thumbnail
    @PutMapping("/api/v1/product-images/product/{productId}/thumbnail/{imageId}")
    public ResponseEntity<ProductImageResponse> setThumbnail(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        var image = productImageService.setThumbnail(productId, imageId);
        var response = productImageMapper.toResponse(image);
        return ResponseEntity.ok(response);
    }

    // ==================== SERVE ẢNH TRỰC TIẾP ====================
    
    // Serve ảnh trực tiếp từ filesystem
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = imagesPath.resolve(filename).normalize();
            
            // Kiểm tra file có tồn tại không
            if (!Files.exists(filePath)) {
                log.warn("Image file not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Kiểm tra file có nằm trong thư mục images không (security check)
            if (!filePath.startsWith(imagesPath)) {
                log.warn("Security violation: Attempted to access file outside images directory: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            
            // Xác định content type
            String contentType = determineContentType(filename);
            
            log.info("Serving image: {} with content type: {}", filename, contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error serving image: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Health check cho thư mục images
    @GetMapping("/images/health")
    public ResponseEntity<String> imagesHealthCheck() {
        try {
            if (Files.exists(imagesPath)) {
                return ResponseEntity.ok("Images directory exists and is accessible");
            } else {
                return ResponseEntity.status(500).body("Images directory does not exist");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error accessing images directory: " + e.getMessage());
        }
    }

    // Helper method để xác định content type
    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
        }
    }
} 