package com.fy.weblog.interceptors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.NonNull;

import cn.hutool.core.util.StrUtil;

// 真正的「刷新令牌拦截器」：只负责续期，不做任何拦截
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;
    // 登录Token的过期时间（比如30分钟，和登录时保持一致）
    private static final Long LOGIN_USER_TTL = 30L;
    // Redis中存储用户的Key前缀（和登录时保持一致）
    private static final String LOGIN_USER_KEY = "login:token:";

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 1. 从请求头获取Token
        String token = request.getHeader("authorization");
        // 2. 如果Token为空，直接放行（交给登录拦截器处理）
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 3. 拼接Redis中存储用户的Key
        String key = LOGIN_USER_KEY + token;
        // 4. 核心操作：续期
        // 刷新拦截器： 给 key 设为 30 分钟后过期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 5. 放行（不管续期成功与否，都不拦截）
        return true;
    }
}