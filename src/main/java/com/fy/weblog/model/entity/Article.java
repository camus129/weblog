package com.fy.weblog.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("article")
public class Article {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String title;
    
    private String content;
    
    private String coverImg;
    
    private String state;
    
    private Integer categoryId;
    
    private Long createUser;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Integer liked;
    
    private Integer collected;
    
    // 非数据库字段，用于返回给前端
    private transient String name;
    private transient String icon;
}
