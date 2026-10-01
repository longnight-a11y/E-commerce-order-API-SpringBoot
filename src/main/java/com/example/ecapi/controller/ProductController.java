package com.example.ecapi.controller;

import com.example.ecapi.dto.pagination.PageResponse;
import com.example.ecapi.dto.product.ProductCreateRequest;
import com.example.ecapi.dto.product.ProductResponse;
import com.example.ecapi.entity.User;
import com.example.ecapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Sell New Product")
    public ProductResponse createProduct(@Valid @RequestBody ProductCreateRequest request, @AuthenticationPrincipal User user){
        return productService.createProduct(request, user);
    }

    @GetMapping
    @Operation(summary = "Get All Product List")
    public PageResponse<ProductResponse> getProducts(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "50") int size){
        return productService.getProducts(page, size);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get Single Product")
    public ProductResponse getSingleProduct(@PathVariable UUID productId){
        return productService.getSingleProduct(productId);
    }

    @GetMapping("/{sellerId}")
    @Operation(summary = "Get Product List of Specific Seller")
    public PageResponse<ProductResponse> getProducts(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "50") int size,
                                                     @PathVariable UUID sellerId){
        return productService.getProductsOfSpecificSeller(page, size, sellerId);
    }
}
