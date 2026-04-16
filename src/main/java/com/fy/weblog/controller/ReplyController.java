package com.fy.weblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fy.weblog.model.dto.ReplyDTO;
import com.fy.weblog.service.ReplyService;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/replies")
public class ReplyController {
    @Autowired
    private ReplyService replyService;
    
    //新增评论
    @PostMapping
    public void saveReply(@RequestBody ReplyDTO replyDTO) {
        replyService.saveReply(replyDTO);
    }
    
    //新增评论回复
    // @PostMapping("/reply")
    // public Result reply(@RequestBody Reply reply) {
    //     return commentService.addReply(reply);
    // }
    
    // //分页获取评论回复
    // @PostMapping("/page")
    // public Result page(@RequestBody ReplyPageQuery query) {
    //     return commentService.page(query);
    // }
}
