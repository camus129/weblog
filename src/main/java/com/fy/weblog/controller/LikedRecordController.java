package com.fy.weblog.controller;

import com.fy.weblog.dto.LikeRecordFormDTO;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.LikedRecord;
import com.fy.weblog.service.LikedRecordService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞记录
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikedRecordController {
    private final LikedRecordService likedRecordService;
    @PostMapping
    //点赞
    public Result like(@RequestBody LikedRecord likeRecord) {
        return likedRecordService.addlikeRecord(likeRecord);
    }
}
