package com.fy.weblog.model.dto;

import lombok.Data;

@Data
public class ReplyDTO {
    private Long articleId;
    private Long userId;
    private String content;
}

