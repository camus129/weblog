package com.fy.weblog.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String phone;
    private String captchaId;
    private String captcha;
    // private String code;
    private String password;
}

