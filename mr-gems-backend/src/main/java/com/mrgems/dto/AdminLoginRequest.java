package com.mrgems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
