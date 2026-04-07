package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.weblog.dto.LoginFormDTO;
import com.fy.weblog.dto.Result;
import com.fy.weblog.entity.User;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {

    // Result<String> sendCode(String phone, HttpSession session);

    // Result<String> loginByCode(LoginFormDTO loginForm, HttpSession session);

    Result<String> loginByPassword(LoginFormDTO loginFormDTO, HttpSession session);

    Result<String> registerByPassword(LoginFormDTO loginFormDTO);

    Map<String, String> createCaptcha();

    Result<String> verifyCaptcha(LoginFormDTO loginFormDTO);

    Result<String> logout(String authHeader);

    Result<String> updateUserInfo(User user);


}
