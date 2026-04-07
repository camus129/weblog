package com.fy.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.weblog.entity.User;
import org.apache.ibatis.annotations.Mapper;  // 改为这个导入

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
}