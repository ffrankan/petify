package com.petify.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String accessToken;
    
    private String refreshToken;
    
    private UserInfoDTO user;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String email;
        private String realName;
        private String avatarUrl;
        private List<String> roles;
    }
}