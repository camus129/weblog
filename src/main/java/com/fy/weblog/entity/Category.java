package com.fy.weblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("category_name")
    private String name;

    @TableField("parent_id")
    private Long parentId;

    private String categoryAlias;
    private Long createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
