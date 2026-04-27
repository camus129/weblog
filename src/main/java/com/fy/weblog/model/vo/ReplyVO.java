package com.fy.weblog.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyVO {
    // @ApiModelProperty("id")
    private Long id;
    // @ApiModelProperty("回答内容")
    private String content;
    // @ApiModelProperty("是否匿名提问")
    // private Boolean anonymity;
    // @ApiModelProperty("是否隐藏")
    //private Boolean hidden;
    // @ApiModelProperty("评论数量")
    private Integer replyTimes;
    // @ApiModelProperty(value = "创建时间，也就是回答时间")
    private LocalDateTime createTime;

    // @ApiModelProperty("当前回复者id")
    private Long userId;
    // @ApiModelProperty("当前回复者昵称")
    private String userName;
    // @ApiModelProperty("当前回复者头像")
    private String userIcon;
    // @ApiModelProperty("当前回复者类型，2-作者，其它-用户")
    private Integer userType;

    // @ApiModelProperty("是否点过赞")
    private Boolean liked;
    // @ApiModelProperty("点赞数量")
    private Integer likedTimes;
    // @ApiModelProperty("目标用户名字")
    private String targetUserName;

    //自行添加的字段
    private Long targetReplyId;
    private Long targetUserId;
}