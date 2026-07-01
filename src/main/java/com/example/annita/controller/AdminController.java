package com.example.annita.controller;

import com.example.annita.dto.AdminDashboardResponse;
import com.example.annita.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin dashboard endpoints")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    public AdminController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MODERATOR')")
    @Operation(summary = "Get dashboard metrics", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminDashboardResponse getDashboard() {
        return adminDashboardService.getDashboard();
    }
}
