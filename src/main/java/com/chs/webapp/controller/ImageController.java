package com.chs.webapp.controller;

import com.chs.webapp.dto.ImageResponse;
import com.chs.webapp.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/product/{productId}/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    /**
     * 上傳圖片到產品
     * POST /v1/product/{productId}/image
     */
    @PostMapping
    public ResponseEntity<ImageResponse> uploadImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Uploading image to product: {}", productId);

        String authenticatedEmail = authentication.getName();
        ImageResponse response = imageService.uploadImage(productId, file, authenticatedEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 取得產品的所有圖片
     * GET /v1/product/{productId}/image
     */
    @GetMapping
    public ResponseEntity<List<ImageResponse>> getProductImages(@PathVariable UUID productId) {
        log.info("Getting images for product: {}", productId);

        List<ImageResponse> images = imageService.getProductImages(productId);
        return ResponseEntity.ok(images);
    }

    /**
     * 取得單一圖片資訊
     * GET /v1/product/{productId}/image/{imageId}
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImageById(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {

        log.info("Getting image: imageId={}, productId={}", imageId, productId);

        ImageResponse response = imageService.getImageById(productId, imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * 刪除圖片
     * DELETE /v1/product/{productId}/image/{imageId}
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            Authentication authentication) {

        log.info("Deleting image: imageId={}, productId={}", imageId, productId);

        String authenticatedEmail = authentication.getName();
        imageService.deleteImage(productId, imageId, authenticatedEmail);

        return ResponseEntity.noContent().build();
    }
}
