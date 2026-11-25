package com.mrgems.service;

import com.mrgems.dto.AdminLoginResponse;
import com.mrgems.dto.AdminProfileResponse;
import com.mrgems.model.Admin;
import com.mrgems.repository.AdminRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AdminService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder ,
            JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Admin findAdminByEmail(String email){
        return adminRepository
                .findByEmail(email)
                .orElseThrow(NoSuchElementException::new);
    }

    public String hashPassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword){
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Admin authenticateAdmin(String email, String rawPassword){
        Admin admin = findAdminByEmail(email);
        if(!admin.isActive()){
            throw new NoSuchElementException();
        }
        if(!verifyPassword(rawPassword, admin.getPassword())){
            throw new BadCredentialsException("Wrong email or password");
        }
        return admin;
    }

    public AdminLoginResponse loginAdmin(String email, String rawPassword){
        Admin admin = authenticateAdmin(email , rawPassword);
        String token = jwtService.generateToken(admin.getEmail());

        AdminLoginResponse response = new AdminLoginResponse();

        response.setAdminId(admin.getId());
        response.setAdminName(admin.getAdminName());
        response.setEmail(admin.getEmail());
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        return response;
    }

    public AdminProfileResponse getAdminProfile(String email){
        Admin admin = findAdminByEmail(email);
        AdminProfileResponse response = new AdminProfileResponse();
        response.setAdminId(admin.getId());
        response.setAdminName(admin.getAdminName());
        response.setEmail(admin.getEmail());
        response.setActive(admin.isActive());
        response.setCreatedAt(admin.getCreatedAt());
        return response;
    }
}
