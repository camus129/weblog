package com.fy.weblog.service.Impl;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.LikedRecord;
import com.fy.weblog.mapper.LikedRecordMapper;
import com.fy.weblog.service.LikedRecordService;
import com.fy.weblog.utils.UserHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements LikedRecordService {
    private final StringRedisTemplate stringRedisTemplate;

    //点赞【含异步更新数据库】
    @Override
    public Result addlikeRecord(LikedRecord likeRecord) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        if(userId == null){
            //用户未登录，无需查询是否点赞
            return Result.ok("未登录，点赞失败");
        }
        //2.判断当前登录用户是否已点赞某业务
        String key = likeRecord.getBizType()+":liked:"+likeRecord.getBizId();

        try{
            Double isMember = stringRedisTemplate.opsForZSet()//ZSet有序集合排序
                .score(key, userId.toString());//查询用户id是否在set点赞列表里
            if(isMember == null){
                //3.如果未点赞，可以点赞
                return recordLike(userId, likeRecord.getBizType(), likeRecord.getBizId(), key);
            }else{
                //4.如果已点赞，取消点赞
                return handleCancelLike(userId, likeRecord.getBizType(), likeRecord.getBizId(), key);
            }
        }catch(Exception e){
            log.error("点赞操作异常", e);
            return Result.fail("点赞失败");
        }
    }

    //点赞
    public Result recordLike(Long userId, String bizType, Long bizId, String key) {
        //1.先更新redis
        boolean updateSucess = stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
        //ZSet有序集合；System.currentTimeMillis()当前时间戳
        if(!updateSucess){
            //更新redis失败，点赞失败
            return Result.fail("点赞失败");
        }
        //设置过期时间
        stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);

        //2.异步更新数据库
        asyncUpdateDatabase(userId, bizType, bizId, true);
        
        //3.异步更新点赞数(使用redis计数器)
        String countKey = bizType+":"+bizId+":liked_count";
        stringRedisTemplate.opsForValue().increment(countKey,1);

        return Result.ok("点赞成功");
    }

    //取消点赞
    public Result handleCancelLike(Long userId, String bizType, Long bizId, String key) {
        //1.先更新redis
        Long updateSucess = stringRedisTemplate.opsForZSet().remove(key,userId.toString());
        if(updateSucess == null || updateSucess <= 0){
            //更新redis失败，取消点赞失败
            return Result.fail("取消点赞失败");
        }
        //2.异步更新数据库
        asyncUpdateDatabase(userId, bizType, bizId, false);
        //3.异步更新点赞数(使用redis计数器)
        String countKey = bizType+":"+bizId+":liked_count";
        stringRedisTemplate.opsForValue().decrement(countKey,1);
        return Result.ok("取消点赞成功");
    }
    
    /**
     * 异步更新数据库
     */
    @Async //：开启异步
    public void asyncUpdateDatabase(Long userId, String bizType, Long bizId, boolean isLike) {
        try {
            if (isLike) {
                // 点赞：插入记录
                LikedRecord record = new LikedRecord();
                record.setUserId(userId);
                record.setBizType(bizType);
                record.setBizId(bizId);
                record.setCreateTime(LocalDateTime.now());
                save(record);
                
                // 原子更新点赞数
                update().setSql("liked = liked + 1").eq("bizId",bizId).update();
            } else {
                // 取消点赞：删除记录
                LambdaQueryWrapper<LikedRecord> wrapper = new LambdaQueryWrapper<>();//【条件查询器】
                wrapper.eq(LikedRecord::getUserId, userId)
                    .eq(LikedRecord::getBizType, bizType)
                    .eq(LikedRecord::getBizId, bizId);
                remove(wrapper);
                
                // 原子减少点赞数
                update().setSql("liked = liked - 1").eq("bizId",bizId).update();
            }
        } catch (Exception e) {
            log.error("异步更新数据库失败", e);
            // 这里可以加入重试机制或记录到死信队列
        }
    }

}
