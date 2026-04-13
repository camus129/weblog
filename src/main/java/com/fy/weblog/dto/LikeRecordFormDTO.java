package com.fy.weblog.dto;
import lombok.Data;

@Data
public class LikeRecordFormDTO {
    /**
     * 关联的业务id
     */
    private Long bizId;
    /**
     * 关联的业务类型
     */
    private String bizType;
    /**
     * 是否点赞
     */
    private Boolean isLiked;
}
