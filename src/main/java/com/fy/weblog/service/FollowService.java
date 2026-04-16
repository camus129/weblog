package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.Follow;

import org.springframework.stereotype.Service;

@Service
public interface FollowService extends IService<Follow> {

    Result follow(Long followUserId,Boolean isFollow);

    Result isFollow(Long followUserId);

}
