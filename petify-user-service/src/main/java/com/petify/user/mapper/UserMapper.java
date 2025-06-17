package com.petify.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petify.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}