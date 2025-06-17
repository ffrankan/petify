package com.petify.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {
    
    @NotBlank(message = "登录标识不能为空")
    private String identifier;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}