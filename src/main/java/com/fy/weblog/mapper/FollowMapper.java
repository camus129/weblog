package com.fy.weblog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.weblog.entity.Follow;

// @Repository
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    
}