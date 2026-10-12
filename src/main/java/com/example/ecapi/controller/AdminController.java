package com.example.ecapi.controller;

import com.example.ecapi.dto.category.CategoryResponse;
import com.example.ecapi.dto.category.CategoryUpdateRequest;
import com.example.ecapi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping("/category/{categoryId}")
    @Operation(summary = "Update Category (ADMIN only)")
    public CategoryResponse updateCategory(@RequestBody CategoryUpdateRequest request,
                                           @PathVariable UUID categoryId,
                                           @AuthenticationPrincipal Jwt jwt){
        return adminService.updateCategory(request, categoryId, jwt);
    }
}
