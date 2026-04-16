package com.fy.weblog.service.Impl;

import org.springframework.stereotype.Service;

import com.fy.weblog.model.dto.ReplyDTO;
import com.fy.weblog.service.ReplyService;
import com.fy.weblog.utils.UserHolder;

@Service
public class ReplyServiceImpl implements ReplyService {

    @Override
    public void saveReply(ReplyDTO replyDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveReply'");
    }

    // @Override
    // public void saveReply(ReplyDTO replyDTO) {
    //     // 1.获取登录用户
    //     Long userId = UserHolder.getUser().getId();
    //     // 2.数据转换
    //     InteractionReply reply = BeanUtils.toBean(replyDTO, InteractionReply.class);
    //     // 3.补充数据
    //     reply.setUserId(userId);
    //     // 4.保存问题
    //     save(reply);
        
    // }
}
