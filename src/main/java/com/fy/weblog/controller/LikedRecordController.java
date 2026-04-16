package com.fy.weblog.controller;

import com.fy.weblog.model.dto.LikeRecordFormDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.LikedRecord;
import com.fy.weblog.service.LikedRecordService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    //查询指定业务id点赞列表(查询该用户点过赞的业务 ID 集合
    @GetMapping("/list")
    public Set<Long> getLikeList(@RequestParam("bizId") List<Long> bizId) {
        return likedRecordService.getLikeList(bizId);
    }
}
