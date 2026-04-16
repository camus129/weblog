package com.fy.weblog.interceptors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

import com.fy.weblog.model.dto.UserDTO;
import com.fy.weblog.utils.UserHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

// 登录拦截器：负责校验用户登录状态，并将用户信息存入 UserHolder
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
    private StringRedisTemplate stringRedisTemplate;

    // Redis中存储用户的Key前缀
    private static final String LOGIN_USER_KEY = "login:token:";
    // 登录Token的过期时间（30分钟）
    private static final Long LOGIN_USER_TTL = 30L;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 1. 从请求头获取Token（支持 authorization 和 Authorization）
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getHeader("Authorization");
        }
        log.info("请求路径: {}, Token: {}", request.getRequestURI(), token);
        // 2. 如果Token为空，拦截
        if (StrUtil.isBlank(token)) {
            log.warn("Token为空，拦截请求");
            response.setStatus(401);
            return false;
        }
        // 3. 拼接Redis中存储用户的Key
        String key = LOGIN_USER_KEY + token;
        log.info("Redis key: {}", key);
        // 4. 从Redis中获取用户信息
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        log.info("从Redis获取的用户信息: {}", userMap);
        // 5. 判断用户是否存在
        if (userMap.isEmpty()) {
            log.warn("Redis中未找到用户信息，拦截请求");
            response.setStatus(401);
            return false;
        }
        // 6. 将查询到的Hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 7. 将用户信息存入UserHolder（封装 ThreadLocal 的工具类 ）
        UserHolder.saveUser(userDTO);
        // 8. 刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 9. 放行
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        // 请求结束后移除用户信息，防止内存泄漏
        UserHolder.removeUser();
    }
}
