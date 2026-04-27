package com.fy.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.fy.weblog.model.dto.LoginFormDTO;
import com.fy.weblog.model.dto.PasswordUpdateDTO;
import com.fy.weblog.model.dto.Result;
import com.fy.weblog.model.entity.User;


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

    Result<String> update(User user);

/*修改密码*/
    Result<String> updatePassword(PasswordUpdateDTO passwordDTO, String token);
}
