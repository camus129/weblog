package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.model.dto.PageDTO;
import com.fy.weblog.model.dto.ReplyDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.Reply;
import com.fy.weblog.model.query.ReplyPageQuery;
import com.fy.weblog.model.vo.ReplyVO;

public interface ReplyService extends IService<Reply> {
    Result<Reply> saveReply(ReplyDTO replyDTO);

    PageDTO<ReplyVO> replyPage(ReplyPageQuery query);

    ReplyVO queryReplyById(Long id);

    Result<Reply> saveSonReply(ReplyDTO replyDTO);

}