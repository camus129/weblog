package com.fy.weblog.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.Follow;
import com.fy.weblog.mapper.FollowMapper;
import com.fy.weblog.service.FollowService;
import com.fy.weblog.utils.UserHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final FollowMapper followMapper;

    
    //关注
    @Override
    public Result follow(Long followUserId,Boolean isFollow) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        //2.判断是关注还是取关
        if(isFollow){
            //3.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            //把对象存到数据库里
            save(follow);
            //3.1.从set集合中添加用户
            String followingKey = "user:following:" + userId;  // 用户的关注列表
            String followersKey = "user:followers:" + followUserId;  // 用户的粉丝列表
            //当前用户关注数 +1
            redisTemplate.opsForSet().add(followingKey, followUserId.toString());
            //被关注用户粉丝数 +1
            redisTemplate.opsForSet().add(followersKey, userId.toString());
            
            return Result.ok("关注成功");

        }else{
            //4.取关，删除数据：DELETE FROM follow WHERE user_id = ? AND follow_user_id = ?
            remove(new QueryWrapper<Follow>()  //new QueryWrapper==where,是MyBatis-Plus​的核心类
                .eq("user_id",userId).eq("follow_user_id",followUserId));
            //4.2.从set集合中删除用户
            String followingKey = "user:following:" + userId;  // 用户的关注列表
            String followersKey = "user:followers:" + followUserId;  // 用户的粉丝列表
            //删除用户关注列表中的用户
            redisTemplate.opsForSet().remove(followingKey, followUserId.toString());
            //删除用户粉丝列表中的用户
            redisTemplate.opsForSet().remove(followersKey, userId.toString());
            
            return Result.ok("取关成功");
        }
    }

    //是否已关注
    @Override
    public Result isFollow(Long followUserId) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        String followingKey = "user:following:" + userId;
        //2.使用 Set 的 isMember 方法，O(1) 时间复杂度
        Boolean isMember = redisTemplate.opsForSet()
            .isMember(followingKey, followUserId.toString());
        
        if (Boolean.TRUE.equals(isMember)) {
            return Result.ok(true);  // 在关注列表中
        } else if (Boolean.FALSE.equals(isMember)) {
            return Result.ok(false); // 明确不在关注列表中
        }
    
        //3.Redis 中不存在，查数据库
        // 查询是否关注 select * from follow where user_id = ? and follow_user_id = ?
        Integer count = query()  //自动创建 QueryWrapper<Follow>
            .eq("user_id",userId).eq("follow_user_id",followUserId).count().intValue(); //.count()统计数量；.intValue()转为int类型
        return Result.ok(count > 0);
    }
}
