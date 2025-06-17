package com.petify.user.service.impl;

import com.petify.common.exception.BusinessException;
import com.petify.user.dto.LoginResponseDTO;
import com.petify.user.entity.User;
import com.petify.user.mapper.UserMapper;
import com.petify.user.mapper.UserRoleMapper;
import com.petify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public LoginResponseDTO.UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        List<String> roles = userRoleMapper.selectRolesByUserId(userId);
        
        return new LoginResponseDTO.UserInfoDTO(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRealName(), user.getAvatarUrl(), roles
        );
    }
    
    @Override
    public void updateUserProfile(Long userId, User updateRequest) {
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        existingUser.setRealName(updateRequest.getRealName());
        existingUser.setPhone(updateRequest.getPhone());
        existingUser.setAvatarUrl(updateRequest.getAvatarUrl());
        
        userMapper.updateById(existingUser);
        log.info("用户资料更新成功: {}", userId);
    }
    
    @Override
    public Object getAllUsers() {
        return userMapper.selectList(null);
    }
}