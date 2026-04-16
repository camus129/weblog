package com.fy.weblog.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Reply {
    //【雪花算法】
    @TableId(type = IdType.ASSIGN_ID)
    /* 
     * 评论id
    */
    private Long id;
    /* 
     * 文章id
    */
    private Integer articleId;
    /* 
     * 用户id
    */
    private Long userId;
    /* 
     * 评论内容
    */
    private String content;
    /* 
     * 创建时间
    */
    private LocalDateTime createTime;
    /* 
     * 更新时间
    */
    private LocalDateTime updateTime;
}
