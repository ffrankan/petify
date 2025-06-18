package com.petify.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petify.common.exception.BusinessException;
import com.petify.user.dto.*;
import com.petify.user.entity.User;
import com.petify.user.entity.UserRole;
import com.petify.user.mapper.UserMapper;
import com.petify.user.mapper.UserRoleMapper;
import com.petify.user.service.AuthService;
import com.petify.user.service.RedisService;
import com.petify.user.util.JwtUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    
    @Override
    @Transactional
    public void register(UserRegisterDTO registerDTO) {
        if (userExists(registerDTO.getUsername(), registerDTO.getEmail())) {
            throw new BusinessException(400, "用户名或邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setStatus(1);
        
        userMapper.insert(user);
        
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleName("USER");
        userRoleMapper.insert(userRole);
        
        log.info("用户注册成功: {}", user.getUsername());
    }
    
    @Override
    public LoginResponseDTO login(UserLoginDTO loginDTO, String deviceId, String ipAddress) {
        String identifier = loginDTO.getIdentifier();
        
        if (isLoginLocked(identifier)) {
            throw new BusinessException(423, "登录失败次数过多，请30分钟后重试");
        }
        
        User user = findUserByIdentifier(identifier);
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordFailedLogin(identifier);
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账户已被禁用");
        }
        
        clearFailedLogin(identifier);
        
        List<String> roles = userRoleMapper.selectRolesByUserId(user.getId());
        
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), deviceId, ipAddress);
        
        storeRefreshToken(user.getId(), refreshToken, deviceId);
        
        LoginResponseDTO.UserInfoDTO userInfo = new LoginResponseDTO.UserInfoDTO(
                user.getId(), user.getUsername(), user.getEmail(), 
                user.getRealName(), user.getAvatarUrl(), roles
        );
        
        log.info("用户登录成功: {}", user.getUsername());
        return new LoginResponseDTO(accessToken, refreshToken, userInfo);
    }
    
    @Override
    public String refreshToken(String refreshToken, String deviceId, String ipAddress) {
        try {
            if (!jwtUtil.isTokenValid(refreshToken)) {
                throw new BusinessException(401, "刷新令牌无效");
            }

            if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
                throw new BusinessException(401, "令牌类型错误");
            }
            
            if (isTokenBlacklisted(jwtUtil.getJwtId(refreshToken))) {
                throw new BusinessException(401, "刷新令牌已失效");
            }
            
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new BusinessException(401, "用户状态异常");
            }
            
            List<String> roles = userRoleMapper.selectRolesByUserId(userId);
            return jwtUtil.generateAccessToken(userId, user.getUsername(), roles);
            
        } catch (Exception e) {
            throw new BusinessException(401, "刷新令牌失败");
        }
    }
    
    @Override
    public void logout(String accessToken, String refreshToken) {
        try {
            if (StrUtil.isNotBlank(accessToken)) {
                String accessJti = jwtUtil.getJwtId(accessToken);
                blacklistToken(accessJti, jwtUtil.getExpirationFromToken(accessToken).getTime());
            }
            
            if (StrUtil.isNotBlank(refreshToken)) {
                String refreshJti = jwtUtil.getJwtId(refreshToken);
                blacklistToken(refreshJti, jwtUtil.getExpirationFromToken(refreshToken).getTime());
                
                Long userId = jwtUtil.getUserIdFromToken(refreshToken);
                removeRefreshToken(userId, refreshToken);
            }
            
            log.info("用户登出成功");
        } catch (Exception e) {
            log.error("登出处理异常", e);
        }
    }
    
    private boolean userExists(String username, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).or().eq(User::getEmail, email);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    private User findUserByIdentifier(String identifier) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, identifier).or().eq(User::getEmail, identifier);
        return userMapper.selectOne(wrapper);
    }
    
    private boolean isLoginLocked(String identifier) {
        String key = "login_attempts:" + identifier;
        Integer attempts = redisService.get(key, Integer.class);
        return attempts != null && attempts >= 5;
    }
    
    private void recordFailedLogin(String identifier) {
        String key = "login_attempts:" + identifier;
        Integer attempts = redisService.get(key, Integer.class);
        attempts = attempts == null ? 1 : attempts + 1;
        redisService.set(key, attempts, 30 * 60);
    }
    
    private void clearFailedLogin(String identifier) {
        String key = "login_attempts:" + identifier;
        redisService.delete(key);
    }
    
    private void storeRefreshToken(Long userId, String refreshToken, String deviceId) {
        String key = "refresh_token:" + userId + ":" + deviceId;
        redisService.set(key, refreshToken, 90 * 24 * 60 * 60);
    }
    
    private void removeRefreshToken(Long userId, String refreshToken) {
        String pattern = "refresh_token:" + userId + ":*";
        redisService.deleteByPattern(pattern);
    }
    
    private boolean isTokenBlacklisted(String jti) {
        String key = "blacklist:" + jti;
        return redisService.hasKey(key);
    }
    
    private void blacklistToken(String jti, long expiration) {
        String key = "blacklist:" + jti;
        long ttl = (expiration - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            redisService.set(key, true, (int) ttl);
        }
    }
}