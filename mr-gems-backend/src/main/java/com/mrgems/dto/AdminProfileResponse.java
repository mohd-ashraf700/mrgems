package com.mrgems.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminProfileResponse {
    private Long adminId;
    private String adminName;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;
}
