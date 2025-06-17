package com.petify.user.controller;

import com.petify.common.result.Result;
import com.petify.user.dto.LoginResponseDTO;
import com.petify.user.entity.User;
import com.petify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public Result<LoginResponseDTO.UserInfoDTO> getUserProfile(@RequestHeader("X-User-Id") Long userId) {
        LoginResponseDTO.UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
    
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("X-User-Id") Long userId,
                                     @RequestBody User updateRequest) {
        userService.updateUserProfile(userId, updateRequest);
        return Result.success();
    }
    
    @GetMapping("/admin/users")
    public Result<Object> getAllUsers(@RequestHeader("X-User-Roles") String roles) {
        if (!roles.contains("ADMIN")) {
            return Result.error(403, "权限不足");
        }
        
        Object users = userService.getAllUsers();
        return Result.success(users);
    }
}