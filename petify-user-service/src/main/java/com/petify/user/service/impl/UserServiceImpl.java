package com.petify.user.service.impl;

import com.petify.common.exception.BusinessException;
import com.petify.user.dto.LoginResponseDTO;
import com.petify.user.entity.User;
import com.petify.user.repository.UserRepository;
import com.petify.user.repository.UserRoleRepository;
import com.petify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    
    @Override
    public LoginResponseDTO.UserInfoDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
        
        return new LoginResponseDTO.UserInfoDTO(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRealName(), user.getAvatarUrl(), roles
        );
    }
    
    @Override
    public void updateUserProfile(Long userId, User updateRequest) {
        User existingUser = userRepository.findById(userId).orElse(null);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        existingUser.setRealName(updateRequest.getRealName());
        existingUser.setPhone(updateRequest.getPhone());
        existingUser.setAvatarUrl(updateRequest.getAvatarUrl());
        
        userRepository.save(existingUser);
        log.info("用户资料更新成功: {}", userId);
    }
    
    @Override
    public Object getAllUsers() {
        return userRepository.findAll();
    }
}