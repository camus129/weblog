package com.fy.weblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fy.weblog.interceptors.LoginInterceptor;
import com.fy.weblog.interceptors.RefreshTokenInterceptor;

import jakarta.annotation.Resource;

// 2. 配置类
@Component
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器:顺序很重要
        // registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
        //         .addPathPatterns("/**")         // 拦截所有请求
        //         .excludePathPatterns(           // 可选的排除项
        //                 "/user/register",               // 注册接口
        //                 "/user/login",               // 登录接口
        //                 "/captcha"               // 验证码接口
        //         );
        
        // // 刷新Token拦截器
        // registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
        //         .addPathPatterns("/**");  // 拦截所有请求
    }
}
