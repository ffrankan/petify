package com.petify.user.controller;

import com.petify.common.result.Result;
import com.petify.user.dto.*;
import com.petify.user.service.AuthService;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }
    
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Validated @RequestBody UserLoginDTO loginDTO,
                                         HttpServletRequest request) {
        String deviceId = getDeviceId(request);
        String ipAddress = getIpAddress(request);
        
        LoginResponseDTO response = authService.login(loginDTO, deviceId, ipAddress);
        return Result.success(response);
    }
    
    @PostMapping("/refresh")
    public Result<String> refreshToken(@Validated @RequestBody TokenRefreshDTO refreshDTO,
                                      HttpServletRequest request) {
        String deviceId = getDeviceId(request);
        String ipAddress = getIpAddress(request);
        
        String newAccessToken = authService.refreshToken(refreshDTO.getRefreshToken(), deviceId, ipAddress);
        return Result.success(newAccessToken);
    }
    
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                              @RequestBody(required = false) TokenRefreshDTO refreshDTO) {
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        
        String refreshToken = refreshDTO != null ? refreshDTO.getRefreshToken() : null;
        
        authService.logout(accessToken, refreshToken);
        return Result.success();
    }
    
    private String getDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-ID");
        return deviceId != null ? deviceId : IdUtil.fastSimpleUUID();
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}