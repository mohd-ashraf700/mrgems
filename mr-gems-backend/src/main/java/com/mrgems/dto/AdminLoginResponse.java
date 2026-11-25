package com.mrgems.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class AdminLoginResponse {
    private Long adminId;
    private String adminName;
    private String email;
    private String accessToken;
    private String tokenType;
}
