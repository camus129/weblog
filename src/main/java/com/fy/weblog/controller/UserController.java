package com.fy.weblog.controller;


import com.fy.weblog.model.dto.LoginFormDTO;
import com.fy.weblog.model.dto.PasswordUpdateDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.User;



import jakarta.servlet.http.HttpSession;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fy.weblog.service.UserService;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.system.UserInfo;
import jakarta.annotation.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //声明日志对象
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    /* 
    //发送验证码
    @PostMapping("/sms/send")
    public Result<String> sendCodePost(@RequestParam String phone, HttpSession session) {
        try {
            //发送短信验证码并保存验证码
            Result<String> result = userService.sendCode(phone, session);
            return result;
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return Result.fail("发送验证码失败：" + e.getMessage());
        }
    }

    //短信验证码登录
    @PostMapping("/mobile/login")
    public Result<String> loginByCode(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        return userService.loginByCode(loginForm,session);
        
    }
    */

    //手机号密码登录
    @PostMapping("/mobile/phone/login")
    public Result<String> loginByPassword(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        log.info("手机号密码登录：{}", loginFormDTO);
        return userService.loginByPassword(loginFormDTO, session);
    }

    //手机号密码注册
    @PostMapping("/mobile/phone/register")
    public Result<String> registerByPassword(@RequestBody LoginFormDTO loginFormDTO) {
        log.info("手机号密码注册：{}", loginFormDTO);
        return userService.registerByPassword(loginFormDTO);
    }

    //退出登录
    @PostMapping("/logout")
    public Result<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userService.logout(authHeader);
    }

    //图片验证码- -生成
    @GetMapping("/captcha")
    public Map<String, String> createCaptcha(HttpServletResponse response) throws IOException {
        log.info("创建验证码");
        return userService.createCaptcha();
    }

    //图片验证码 - 验证
    @PostMapping("/captcha/verify")
    public Result<String> verifyCaptcha(@RequestBody LoginFormDTO loginFormDTO) {
        log.info("验证验证码：{}", loginFormDTO);
        return userService.verifyCaptcha(loginFormDTO);
    }

    //根据id查询用户信息
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok("");
        }
        // 返回
        return Result.ok(user);
    }

    //更新用户信息
    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        return userService.update(user);
    }
    

    /*-------------------------------------------------*/
    // 在 UserController 类中添加以下方法
    @PostMapping("/password/update")
    public Result<String> updatePassword(
            @RequestBody PasswordUpdateDTO passwordDTO,
            @RequestHeader("Authorization") String token) {
        log.info("修改密码请求：{}", passwordDTO);
        return userService.updatePassword(passwordDTO, token);
    }

    


}
