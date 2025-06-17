package com.petify.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petify.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    @Select("SELECT role_name FROM user_roles WHERE user_id = #{userId}")
    List<String> selectRolesByUserId(Long userId);
}