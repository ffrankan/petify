package com.petify.user.service;

import com.petify.user.dto.*;

public interface AuthService {
    
    void register(UserRegisterDTO registerDTO);
    
    LoginResponseDTO login(UserLoginDTO loginDTO, String deviceId, String ipAddress);
    
    String refreshToken(String refreshToken, String deviceId, String ipAddress);
    
    void logout(String accessToken, String refreshToken);
}