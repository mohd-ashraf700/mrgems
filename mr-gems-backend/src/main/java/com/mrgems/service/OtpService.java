package com.mrgems.service;

import com.mrgems.model.Admin;
import com.mrgems.model.PasswordResetOtp;
import com.mrgems.repository.AdminRepository;
import com.mrgems.repository.PasswordResetOtpRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class OtpService {
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AdminRepository adminRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    public OtpService(
            PasswordResetOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService ,
            AdminRepository adminRepository
    ){
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.adminRepository = adminRepository;
    }
    private String generateOtp(){
        int otp = secureRandom.nextInt(1_000_000);
        return String.format(Locale.ROOT,"%06d",otp);
    }

    @Transactional
    public void issueOtp(Admin admin){
        LocalDateTime now = LocalDateTime.now();

        Optional<PasswordResetOtp> latestOtp =
                otpRepository.findTopByAdmin_IdOrderByCreatedAtDesc(admin.getId());
        if(latestOtp.isPresent()){
            LocalDateTime resendAllowAt = latestOtp.get().getCreatedAt().plusSeconds(60);

            if(now.isBefore(resendAllowAt)){
                throw new IllegalStateException(
                        "please wait before requesting another otp."
                );
            }
        }

        List<PasswordResetOtp> oldOtps =
                otpRepository.findByAdmin_IdAndUsedFalse(admin.getId());
        oldOtps.forEach(otp -> otp.setUsed(true));
        otpRepository.saveAll(oldOtps);

        String rawOtp = generateOtp();

        PasswordResetOtp passwordResetOtp = new PasswordResetOtp();

        passwordResetOtp.setAdmin(admin);
        passwordResetOtp.setOtpHash(passwordEncoder.encode(rawOtp));
        passwordResetOtp.setExpiresAt(now.plusMinutes(5));
        passwordResetOtp.setAttempts(0);
        passwordResetOtp.setUsed(false);
        passwordResetOtp.setVerified(false);

        otpRepository.save(passwordResetOtp);

        emailService.sendPasswordResetOtp(admin.getEmail(),rawOtp);
    }

    @Transactional
    public Optional<String> verifyOtp(Long adminId , String submittedOtp){
        Optional<PasswordResetOtp> otpOptional =
                otpRepository.findTopByAdmin_IdAndUsedFalseOrderByCreatedAtDesc(adminId);
        if(otpOptional.isEmpty()){
            return Optional.empty();
        }
        PasswordResetOtp otp = otpOptional.get();
        if(otp.isVerified()){
            return Optional.empty();
        }
        if(!LocalDateTime.now().isBefore(otp.getExpiresAt())){
            otp.setUsed(true);
            otpRepository.save(otp);
            return Optional.empty();
        }
        if(otp.getAttempts() >= 5){
            otp.setUsed(true);
            otpRepository.save(otp);
            return Optional.empty();
        }
            boolean isMatch = submittedOtp != null
                && submittedOtp.matches("\\d{6}")
                && passwordEncoder.matches(submittedOtp, otp.getOtpHash());
        if(!isMatch){
            int updatedAttempts = otp.getAttempts() + 1;
            otp.setAttempts(updatedAttempts);
            if(updatedAttempts >= 5){
                otp.setUsed(true);
            }
            otpRepository.save(otp);
            return Optional.empty();
        }
        String resetToken = generateResetToken();
        otp.setVerified(true);
        otp.setResetTokenHash(passwordEncoder.encode(resetToken));
        otp.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);
        return Optional.of(resetToken);
    }

    private String generateResetToken(){
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    @Transactional
    public boolean resetPassword(Long adminId ,
                                 String resetToken ,
                                 String newPassword){
        if(resetToken == null || resetToken.isBlank()
        || newPassword == null || newPassword.isBlank()){
            return false;
        }
        Optional<PasswordResetOtp> otpOptional =
                otpRepository.findTopByAdmin_IdAndUsedFalseOrderByCreatedAtDesc(adminId);
        if(otpOptional.isEmpty()){
            return false;
        }
        PasswordResetOtp otp = otpOptional.get();
        if(!otp.isVerified()){
            return false;
        }
        if(otp.getResetTokenHash() == null
        ||otp.getResetTokenExpiresAt() == null){
            return false;
        }
        if(!LocalDateTime.now().isBefore(otp.getResetTokenExpiresAt())){
            otp.setUsed(true);
            otp.setResetTokenHash(null);
            otp.setResetTokenExpiresAt(null);
            otpRepository.save(otp);
            return false;
        }
        if(!passwordEncoder.matches(resetToken, otp.getResetTokenHash())){
            return false;
        }
        Optional<Admin> adminOptional = adminRepository.findById(adminId);
        if(adminOptional.isEmpty()){
            return false;
        }
        Admin admin = adminOptional.get();

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);

        List<PasswordResetOtp> pendingOtps =
                otpRepository.findByAdmin_IdAndUsedFalse(adminId);
        pendingOtps.forEach(item -> {
                    item.setUsed(true);
                    item.setResetTokenHash(null);
                    item.setResetTokenExpiresAt(null);
                });
        otpRepository.saveAll(pendingOtps);

        return true;
    }
}