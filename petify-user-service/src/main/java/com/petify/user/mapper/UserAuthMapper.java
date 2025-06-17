package com.petify.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petify.user.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {
}