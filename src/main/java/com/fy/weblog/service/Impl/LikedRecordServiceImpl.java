package com.fy.weblog.service.Impl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.constants.MqConstants;
import com.fy.weblog.constants.MqConstants.Keys;
import com.fy.weblog.mapper.LikedRecordMapper;
import com.fy.weblog.model.dto.LikeTimesDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.LikedRecord;
import com.fy.weblog.service.LikedRecordService;
import com.fy.weblog.utils.RabbitMqHelper;
import com.fy.weblog.utils.StrUtil;
import com.fy.weblog.utils.UserHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements LikedRecordService {
    private final StringRedisTemplate stringRedisTemplate;
    //【封装了 RabbitMQ 的消息发送 == 异步更新数据库】
    private final AmqpTemplate amqpTemplate;
    private final RabbitMqHelper rabbitMqHelper;
    
    //点赞【含异步更新数据库】
    @Override
    public Result addlikeRecord(LikedRecord likeRecord) {
        //1.基于前端的参数，判断是执行点赞还是取消点赞
        boolean success = likeRecord.getIsLiked() ? like(likeRecord) : unlike(likeRecord);
        //2.点赞失败，返回
        if (!success) {
            return Result.fail("点赞失败");
        }
        //3.点赞成功，统计点赞数
        Long likedTimes = lambdaQuery()
                .eq(LikedRecord::getBizId, likeRecord.getBizId())
                .count();
        //4.发送MQ通知
        rabbitMqHelper.send(//把对象转为json后消息发送到指定的 交换机
            MqConstants.LIKE_RECORD_EXCHANGE,//交换机
            StrUtil.format(Keys.LIKED_TIMES_KEY_TEMPLATE, likeRecord.getBizType()),//路由键  
            LikeTimesDTO.of(likeRecord.getBizId(), likedTimes));//消息体：要发送的 Java 对象

        return Result.ok("点赞成功");
    }

    private boolean like(LikedRecord likeRecord) {
        //1.查询点赞记录
        Long count = lambdaQuery()
                .eq(LikedRecord::getUserId, UserHolder.getUser().getId())
                .eq(LikedRecord::getBizId, likeRecord.getBizId())
                .count();
        //2.判断是否存在，点赞已存在则直接结束
        if (count > 0) {
            return false;
        }
        //3.点赞不存在，直接新增
        LikedRecord r = new LikedRecord();
        r.setUserId(UserHolder.getUser().getId());
        r.setBizId(likeRecord.getBizId());
        r.setBizType(likeRecord.getBizType());
        r.setIsLiked(likeRecord.getIsLiked());
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        save(r);
        return true;
    }

    private boolean unlike(LikedRecord likeRecord) {
        return remove(new QueryWrapper<LikedRecord>().lambda()
                .eq(LikedRecord::getUserId, UserHolder.getUser().getId())
                .eq(LikedRecord::getBizId, likeRecord.getBizId())
                .eq(LikedRecord::getBizType, likeRecord.getBizType())
                .eq(LikedRecord::getIsLiked, likeRecord.getIsLiked())
        );
    }

    @Override
    public Set<Long> getLikeList(List<Long> bizId) {
        //1.获取登录用户id
        Long userId = UserHolder.getUser().getId();
        //2.查询点赞状态
        List<LikedRecord> records = lambdaQuery()
                .eq(LikedRecord::getUserId, userId)
                .in(LikedRecord::getBizId, bizId)
                .list();
        //3.判断是否存在，点赞已存在则直接结束
        if (records.isEmpty()) {
            return null;
        }
        //4.返回结果
        return records.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
    }

    

}
