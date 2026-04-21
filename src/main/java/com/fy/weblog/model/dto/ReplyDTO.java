package com.fy.weblog.model.dto;

import lombok.Data;

@Data
public class ReplyDTO {
    //文章id
    private Long articleId;
    //目标用户id
    private Long userId;
    //父评论id
    private Long parentId;
    //目标评论id
    private Long targetCommentId;
    //回答内容
    private String content;
}

