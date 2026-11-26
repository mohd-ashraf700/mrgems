package com.mrgems.repository;

import com.mrgems.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {
}
