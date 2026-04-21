package com.fy.weblog.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fy.weblog.model.dto.PageDTO;

import com.fy.weblog.model.dto.ReplyDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.dto.UserDTO;
import com.fy.weblog.model.entity.Reply;
import com.fy.weblog.model.query.ReplyPageQuery;
import com.fy.weblog.model.vo.ReplyVO;
import com.fy.weblog.service.ReplyService;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/comment")
public class ReplyController {
    @Autowired
    private ReplyService replyService;

    //新增评论
    @PostMapping("/save")
    public Result<Reply> saveReply(@RequestBody ReplyDTO replyDTO) {
        return replyService.saveReply(replyDTO);
    }

    //新增评论回复
    @PostMapping("/replies")
    public Result<Reply> saveSonReply(@RequestBody ReplyDTO replyDTO) {
        return replyService.saveSonReply(replyDTO);
    }

    //分页获取评论回复
    @PostMapping("/page")
    public PageDTO<ReplyVO> replyPage(@RequestBody ReplyPageQuery query) {
        return replyService.replyPage(query);
    }

    //根据id查询评论(查看单个评论)
    @PostMapping("/id")
    public ReplyVO queryReplyById(@RequestBody Long id) {
        return replyService.queryReplyById(id);
    }
}