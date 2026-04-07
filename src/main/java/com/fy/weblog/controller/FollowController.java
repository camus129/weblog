package com.fy.weblog.controller;

import com.fy.weblog.dto.Result;
import com.fy.weblog.service.FollowService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {
    private final FollowService followService;

    @PutMapping("{id}/{isFollow}")
    //关注用户
    public Result follow(@PathVariable Long followUserId,@PathVariable Boolean isFollow) {
        return followService.follow(followUserId,isFollow);
    }

    //是否已关注
    @GetMapping("or/not/{id}")
    public Result isFollow(@PathVariable Long followUserId) {
        return followService.isFollow(followUserId);
    }

}
