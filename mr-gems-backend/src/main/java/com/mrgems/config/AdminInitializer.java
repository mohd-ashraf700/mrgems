package com.mrgems.config;


import com.mrgems.model.Admin;
import com.mrgems.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;
    public AdminInitializer(AdminRepository adminRepository
            , PasswordEncoder passwordEncoder ,
                            @Value("${MR_GEMS_ADMIN_NAME}") String adminName,
                            @Value("${MR_GEMS_ADMIN_EMAIL}")String adminEmail,
                            @Value("${MR_GEMS_ADMIN_PASSWORD}")String adminPassword) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }
    @Override
    public void run(String... args) throws Exception {
        if(adminRepository.existsByEmail(adminEmail)){
            System.out.println("Admin already exists");
            return;
        }
            Admin admin = new Admin();
            admin.setAdminName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setActive(true);

            adminRepository.save(admin);
        System.out.println("Admin created");
    }
}
