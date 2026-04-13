package com.fy.weblog.service;
import org.springframework.stereotype.Service;

import com.fy.weblog.dto.LikeRecordFormDTO;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.LikedRecord;

@Service
public interface LikedRecordService {

    Result addlikeRecord(LikedRecord likeRecord);

}
