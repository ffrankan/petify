package com.petify.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class TokenRefreshDTO {
    
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}