package com.fy.weblog.service;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fy.weblog.model.dto.LikeRecordFormDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.LikedRecord;

@Service
public interface LikedRecordService {

    Result addlikeRecord(LikedRecord likeRecord);

    Set<Long> getLikeList(List<Long> bizId);

}
