package com.fy.weblog.service;

import org.springframework.stereotype.Service;

import com.fy.weblog.model.dto.ReplyDTO;

@Service
public interface ReplyService {
    void saveReply(ReplyDTO replyDTO);
    

}
