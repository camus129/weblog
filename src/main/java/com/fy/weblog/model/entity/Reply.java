package com.fy.weblog.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("article_comment")
public class Reply {
    //【雪花算法】
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    //文章id
    private Long articleId;
    //父评论id
    private Long parentId;
    //当前回复者id
    private Long userId;
    //回答内容
    private String content;
    //目标用户id
    private Long targetUserId;
    //目标评论id
    private Long targetCommentId;
    //评论数量
    private Integer replyTimes;
    //点赞数量
    private Integer likedTimes;
    //是否隐藏
    private Boolean hidden;
    //创建时间，也就是回答时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;
}