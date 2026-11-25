package com.mrgems.controller;

import com.mrgems.dto.AdminLoginRequest;
import com.mrgems.dto.AdminLoginResponse;
import com.mrgems.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse>
    adminLoginRequest(@Valid @RequestBody AdminLoginRequest request){
        AdminLoginResponse response = adminService.loginAdmin(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

}
