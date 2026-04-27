package com.fy.weblog.model.entity;
import java.time.LocalDateTime;

import lombok.Data;
@Data
public class LikedRecord {
    /**
     * 点赞主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 被点赞的业务id(某文章id/某评论id)
     */
    private Long bizId;
    /**
     * 被点赞的业务类型
     */
    private String bizType;
    /**
     * 是否点赞
     */
    private Boolean isLiked;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
