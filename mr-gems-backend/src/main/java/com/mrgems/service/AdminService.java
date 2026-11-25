package com.mrgems.service;

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
    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
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
}
