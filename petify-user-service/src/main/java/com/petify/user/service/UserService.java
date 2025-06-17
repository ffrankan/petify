package com.petify.user.service;

import com.petify.user.dto.LoginResponseDTO;
import com.petify.user.entity.User;

public interface UserService {
    
    LoginResponseDTO.UserInfoDTO getUserInfo(Long userId);
    
    void updateUserProfile(Long userId, User updateRequest);
    
    Object getAllUsers();
}