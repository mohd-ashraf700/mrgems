package com.mrgems.repository;

import com.mrgems.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByAdmin_IdAndUsedFalseOrderByCreatedAtDesc(Long adminId);

    Optional<PasswordResetOtp>
    findTopByAdmin_IdOrderByCreatedAtDesc(Long adminId);

    List<PasswordResetOtp>
    findByAdmin_IdAndUsedFalse(Long adminId);
}
