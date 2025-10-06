package com.stu.product_service.service;

import com.stu.product_service.entity.Product;
import com.stu.product_service.entity.ProductImage;

import com.stu.product_service.repository.ProductImageRepository;
import com.stu.product_service.repository.ProductRepository;
import com.stu.product_service.dto.request.CreateProductImageRequest;
import com.stu.product_service.dto.request.UpdateProductImageRequest;
import com.stu.product_service.dto.response.ProductImageResponse;
import com.stu.product_service.mapper.ProductImageMapper;
import com.stu.product_service.exception.ProductServiceException;
import com.stu.product_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductImageMapper productImageMapper;

    // Lấy tất cả ảnh chung của sản phẩm (không lấy ảnh của biến thể)
    public List<ProductImage> getProductImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return productImageRepository.findByProductId(productId);
    }

    // Lấy tất cả ảnh của một biến thể
//    public List<ProductImage> getVariantImages(Long variantId) {
//        if (!productVariantRepository.existsById(variantId)) {
//            throw new ProductServiceException(ErrorCode.VARIANT_NOT_FOUND);
//        }
//        return productImageRepository.findByProductVariantId(variantId);
//    }

    // Lấy ảnh thumbnail của sản phẩm
    public ProductImage getProductThumbnail(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Optional<ProductImage> thumbnail = productImageRepository
            .findByProductIdAndIsThumbnailTrue(productId);
        return thumbnail.orElse(null);
    }

    // Lấy ảnh thumbnail của biến thể
//    public ProductImage getVariantThumbnail(Long variantId) {
//        if (!productVariantRepository.existsById(variantId)) {
//            throw new ProductServiceException(ErrorCode.VARIANT_NOT_FOUND);
//        }
//        Optional<ProductImage> thumbnail = productImageRepository
//            .findByProductVariantIdAndIsThumbnailTrue(variantId);
//        return thumbnail.orElse(null);
//    }

//    // Tạo ảnh mới
//    @Transactional
//    public ProductImage createImage(CreateProductImageRequest request) {
//        // Kiểm tra sản phẩm tồn tại
//        Product product = productRepository.findById(request.getProductId())
//            .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));
//
//            // Kiểm tra URL duy nhất trong cùng 1 sản phẩm
//        if (productImageRepository.existsByProductIdAndUrl(request.getProductId(), request.getUrl())) {
//            throw new ProductServiceException(ErrorCode.IMAGE_ALREADY_EXISTS);
//            }
//
//
//        // Nếu đánh dấu là thumbnail, bỏ đánh dấu thumbnail cũ
//        productImageRepository.findByProductIdAndIsThumbnailTrue(product.getId())
//                .ifPresent(oldThumbnail -> {
//                        oldThumbnail.setIsThumbnail(false);
//                        productImageRepository.save(oldThumbnail);
//                    });
//        // Lưu giá trị
//        ProductImage image = ProductImage.builder()
//            .product(product)
//            .url(request.getUrl())
//            .isThumbnail(request.getIsThumbnail())
//            .build();
//        productImageRepository.save(image);
//        return image;
//    }

    // ✅ Upload file thực tế
    @Transactional
    public ProductImage uploadImage(MultipartFile file, Long productId, Boolean isThumbnail) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND));

        // Kiểm tra file
        if (file.isEmpty()) {
            throw new ProductServiceException(ErrorCode.INVALID_INPUT);
        }

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ProductServiceException(ErrorCode.INVALID_INPUT, "Chỉ chấp nhận file hình ảnh");
        }

        // Tạo tên file duy nhất
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Lưu file vào thư mục
        try {
            Path uploadDir = Paths.get("/app/images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new ProductServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể lưu file: " + e.getMessage());
        }

        // Nếu đánh dấu là thumbnail, bỏ đánh dấu thumbnail cũ
        if (Boolean.TRUE.equals(isThumbnail)) {
            productImageRepository.findByProductIdAndIsThumbnailTrue(product.getId())
                    .ifPresent(oldThumbnail -> {
                            oldThumbnail.setIsThumbnail(false);
                            productImageRepository.save(oldThumbnail);
                        });
        }

        // Lưu thông tin ảnh vào database
        ProductImage image = ProductImage.builder()
            .product(product)
            .url(uniqueFilename)
            .isThumbnail(isThumbnail)
            .build();
        productImageRepository.save(image);
        return image;
    }

    // Cập nhật ảnh
    @Transactional
    public ProductImage updateImage(Long imageId, UpdateProductImageRequest request) {
        // 1.kiểm tra xem ảnh cần upadate đã tồn tại chưa
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.IMAGE_NOT_FOUND));

        // Kiểm tra URL duy nhất khi cập nhật URL
        if (request.getUrl() != null && !request.getUrl().equals(image.getUrl())) {
                // Kiểm tra URL duy nhất trong cùng 1 sản phẩm (trừ ảnh hiện tại)
                if (productImageRepository.existsByProductIdAndUrl(image.getProduct().getId(), request.getUrl())) {
                    throw new ProductServiceException(ErrorCode.IMAGE_ALREADY_EXISTS,
                        "Image URL already exists for this product: " + request.getUrl());
                }
            }
        image.setUrl(request.getUrl());

        // đánh dáu là ảnh đại diện
        if (request.getIsThumbnail() != null) {
            image.setIsThumbnail(request.getIsThumbnail());
            
            // Nếu đánh dấu là thumbnail, bỏ đánh dấu thumbnail cũ
            if (Boolean.TRUE.equals(request.getIsThumbnail())) {
                    // Bỏ đánh dấu thumbnail cũ của product
                productImageRepository.findByProductIdAndIsThumbnailTrue(image.getProduct().getId())
                        .ifPresent(oldThumbnail -> {
                            if (!oldThumbnail.getId().equals(imageId)) {
                                oldThumbnail.setIsThumbnail(false);
                                productImageRepository.save(oldThumbnail);
                            }
                        });
            }
        }
        productImageRepository.save(image);
        return image;
    }

    // Xóa ảnh
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.IMAGE_NOT_FOUND));
        
        // Xóa file thực tế nếu có
        try {
            Path filePath = Paths.get("/app/images", image.getUrl());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log lỗi nhưng không throw exception vì có thể file đã bị xóa
            System.err.println("Không thể xóa file: " + e.getMessage());
        }
        
        productImageRepository.deleteById(imageId);
    }

    // Lấy chi tiết ảnh
    public ProductImage getImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.IMAGE_NOT_FOUND));
        return image;
    }

    // Đếm số ảnh của sản phẩm
    public long countProductImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return productImageRepository.countByProductId(productId);
    }

    // ✅ Đặt ảnh làm thumbnail
    @Transactional
    public ProductImage setThumbnail(Long productId, Long imageId) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Kiểm tra ảnh tồn tại và thuộc về sản phẩm
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ProductServiceException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getProduct().getId().equals(productId)) {
            throw new ProductServiceException(ErrorCode.INVALID_INPUT, "Ảnh không thuộc về sản phẩm này");
        }

        // Bỏ đánh dấu thumbnail cũ
        productImageRepository.findByProductIdAndIsThumbnailTrue(productId)
                .ifPresent(oldThumbnail -> {
                    if (!oldThumbnail.getId().equals(imageId)) {
                        oldThumbnail.setIsThumbnail(false);
                        productImageRepository.save(oldThumbnail);
                    }
                });

        // Đặt ảnh mới làm thumbnail
        image.setIsThumbnail(true);
        productImageRepository.save(image);
        return image;
    }
} 