package com.fy.weblog.utils;

import exception.BusinessException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    //构造方法，注入Redis存储器
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //以JSON格式存入redis
    public void set(String key,Object value,Long time,TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(value),time,unit);
    }

    //主动更新缓存【穿透：逻辑过期时间】
    public void setWithLogicalExpire(String key,Object value,Long time,TimeUnit unit){
        //设置逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //写入Redis【更新key和数据】
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }

    //【穿透】
    public <R,ID> R queryWithPassThrough(  //R任意返回值；T任意类型；<K键,V值>
        String keyPrefix,      // ：key前缀
        ID id,                 // ：ID值
        Class<R> type,         // ：返回类型
        Long time,             // ：过期时间数值
        TimeUnit unit,         // ：过期时间单位
        Function<ID,R> dbFallback  // ：数据库查询回调函数
    ){  
        String key = keyPrefix + id;
        //1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中 （判断缓存有效数据
        if(StrUtil.isNotBlank(shopJson)){
            //3.命中，直接返回缓存中的数据
            R r = JSONUtil.toBean(shopJson, type);
            return r;
        }
        //4.未命中，根据id查询数据库
        R r = dbFallback.apply(id);
        //5.数据库 不存在，返回错误
        if(r == null){
            //5.1 数据库查询结果为空，写入空值到redis【缓存空值，设置过期时间，解决缓存穿透问题】
            this.set(key,"",time,unit);
            //5.2 返回错误
            return null;
        }
        //6.数据库 存在，写入redis
        this.set(key,r,time,unit);
        //7.返回数据库查询结果
        return r;
    }

    //创建线程
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    //开锁和关锁
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    //【击穿：互斥锁-更新key逻辑有效期】
    public <R,ID> R queryWithLogicalExpire(
        String keyPrefix,ID id,Class<R> type,Function<ID,R> dbFallback
        ,Long time,TimeUnit unit
    ){
        String key = keyPrefix + id;
        //1.从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        //1.1.缓存未命中（去数据库中找《缓存空值，防止穿透》
        if(StrUtil.isBlank(json)){  //为空白
            //1.1.1查数据库
            R r = dbFallback.apply(id);
            if(r == null){
                //1.1.2 数据库没有，写入空值到redis
                this.set(key,"",time,unit);
                //返回错误
                return null;
            }
            //1.1.3 数据库有，写入redis
            this.setWithLogicalExpire(key,r,time,unit);
            return r;
        }
        
        //1.2.缓存命中
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);//json转为对象
        R r = JSONUtil.toBean(JSONUtil.toJsonStr(redisData.getData()), type);//商铺序列化信息
        LocalDateTime expireTime = redisData.getExpireTime();//设置逻辑过期时间
        //2.判断缓存key是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //2.1.缓存未过期，返回商铺信息
            return r;
        }
        //2.2.缓存过期
        String lockKey = "lock:shop:" + id;
        boolean isLock = tryLock(lockKey);
        //3.锁是否开放
        //3.1.锁开放，《开独立线程：异步重建缓存》
        if(isLock){
            //开独立线程
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try{
                    //查询数据库（更新最新数据）
                    R rNew = dbFallback.apply(id);
                    //数据库 不存在，写入空值到redis【缓存空值，防止穿透】
                    if(rNew == null){
                        this.set(key,"",time,unit);
                    }else{
                        //数据库 存在，写入redis
                        this.setWithLogicalExpire(key,rNew,time,unit);
                    }
                }catch(Exception e){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缓存重建失败", HttpStatus.INTERNAL_SERVER_ERROR);
                }finally{
                    //释放锁
                    unlock(lockKey);
                }
                   
            });
        }
        //3.2.锁不开放，返回信息《直接返回旧数据》
        return r;
    }
    
}













