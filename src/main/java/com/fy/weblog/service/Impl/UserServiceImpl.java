package com.fy.weblog.service.Impl;

import com.fy.weblog.config.RedisConfig;
import com.fy.weblog.dto.LoginFormDTO;
import com.fy.weblog.dto.Result;
import com.fy.weblog.dto.UserDTO;
import com.fy.weblog.entity.User;
import com.fy.weblog.handler.GlobalExceptionHandler;
import com.fy.weblog.mapper.UserMapper;
import com.fy.weblog.service.UserService;

import com.fy.weblog.utils.CacheClient;
import com.fy.weblog.utils.MD5Util;
import com.fy.weblog.utils.RedisConstants;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import io.micrometer.common.util.StringUtils;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;//hutool依赖的BeanUtil
import com.fy.weblog.utils.RegexUtils;
import com.fy.weblog.utils.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RedisConfig redisConfig;
    private final GlobalExceptionHandler globalExceptionHandler;
    @Autowired
    private UserMapper userMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 声明日志对象
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    UserServiceImpl(CacheClient cacheClient, GlobalExceptionHandler globalExceptionHandler, RedisConfig redisConfig) {
        //this.cacheClient = cacheClient;
        this.globalExceptionHandler = globalExceptionHandler;
        this.redisConfig = redisConfig;
    }

    /*
    //发送验证码
    @Override
    public Result<String> sendCode(String phone, HttpSession session) {
        try {
            //1.校验手机号
            if(RegexUtils.isPhoneInvalid(phone)){
                log.info("手机号格式错误：{}", phone);
                return Result.fail("手机号格式错误");
            }
            //2.符合，生成验证码
            String code = RandomUtil.randomNumbers(6);
            //3.保存验证码到redis
            stringRedisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
            //4.发送验证码（模拟）
            log.info("发送验证码成功，验证码为：{}", code);
            //正常返回
            return Result.ok("验证码发送成功");
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return Result.fail("发送验证码失败：" + e.getMessage());
        }
    }

    //验证码登录
    @Override
    public Result<String> loginByCode(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号格式
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //1.1不符合，返回
            return Result.fail("手机号格式错误！");
        }
        //2.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get("sms:code:"+phone);
        String code = loginForm.getCode();//前端返回用户输入的验证码
        //2.1不一致，报错
        if(cacheCode==null || !cacheCode.equals(code)){
            return Result.fail("验证码错误");
        }
        //3.一致，根据手机号查询用户(MyBatis自动生成响应的SQL语句)
        User user = query().eq("phone",phone).one();
        //4.用户不存在，创建新用户
        if(user == null){
            //TODO 验证码注册时未传入密码，使用默认密码123456
            user = createUserWithPhone(phone,"123456");
        }
        //5.调用createToken方法，生成token
        String token = createToken(user);
        
        //6.返回token给前端
        return Result.ok(token);
    }
    */

    //保存用户到redis里并生成token（登录）
    private String createToken(User user){
        //5.将用户信息保存在redis中
        //5.1.生成用户唯一token（String）（登录授权）
        UUID randomUuid = UUID.randomUUID();
        String token = randomUuid.toString().replace("-", "");
        //5.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);//复制到DTO
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),//userDTO转换为new HashMap
        CopyOptions.create() //自定义转换行为
                .setIgnoreNullValue(true) //忽略null值
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())); //将所有字段值转换为字符串
        //5.3.用户存入redis【HashMap】
        String tokenKey = RedisConstants.LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //opsForHash().putAll：存多字段键值对饼覆盖
        //5.4.token存入redis并设置有效期【String】
        stringRedisTemplate.expire(tokenKey,RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);
        
        return token;
    } 
    

    //创建新用户
    private User createUserWithPhone(String phone,String password){
        //1.创建新用户对象
        User user = new User();
        user.setPhone(phone);
        //为新用户设置随机默认昵称
        user.setNickName("用户"+RandomUtil.randomString(5));
        //密码【加密】存储
        String encryptePwd;
        try {
            encryptePwd = MD5Util.getEncryptedPwd(password);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
        user.setPassword(encryptePwd);
        user.setCreateTime(LocalDateTime.now());  // 添加创建时间
        user.setUpdateTime(LocalDateTime.now());  // 添加更新时间
        
        //2.插入到数据库
        save(user);
        //相当于：this.save(user);  继承父类ServiceImpl的save方法
        return user;
    }

    //密码登录
    @Override
    public Result<String> loginByPassword(LoginFormDTO loginFormDTO, HttpSession session) {
        //TODO
        //String captcha = session.getAttribute(loginFormDTO.getUnique()).toString();
        //1.校验手机号格式
        String phone = loginFormDTO.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误！");
        }
        
        //2.检查登录失败次数（防止暴力破解）
        String failKey = "login:fail:" + phone;
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        Integer failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        if(failCount >= 5) {
            // 获取剩余过期时间
            Long expire = stringRedisTemplate.getExpire(failKey, TimeUnit.SECONDS);
            if(expire != null && expire > 0) {
                return Result.fail(String.format("登录失败次数过多，请%d分%d秒后再试", 
                    expire / 60, expire % 60));
            } else {
                // 已过期，清除计数器
                stringRedisTemplate.delete(failKey);
            }
        }

        //3.在数据库找用户
        User user = query().eq("phone", phone).one();
        if(user == null){
            // 用户不存在也计入失败次数
            stringRedisTemplate.opsForValue().increment(failKey, 1);
            return Result.fail("用户不存在");
        }

        //4.校验密码
        //4.1.从数据库查询用户密码
        String password = user.getPassword();
        String inputPassword = loginFormDTO.getPassword();//前端用户输入的密码
        //4.2.1.将前端用户输入的密码【加密】
        boolean isValid;
        try {
            // 使用【validPassword方法验证】
            isValid = MD5Util.validPassword(inputPassword, password);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
        //4.2.校验密码是否一致
        if(password==null || !isValid){
            // 密码错误，记录失败次数
            stringRedisTemplate.opsForValue().increment(failKey, 1);
            return Result.fail("密码错误");
        }
        
        //5.登录成功
        //5.1 清除失败计数器
        stringRedisTemplate.delete(failKey);
        //5.2.调用createToken方法，生成token
        String token = createToken(user);
        
        return Result.ok("登录成功",token);
    }
    
    //密码注册
    @Override
    public Result<String> registerByPassword(LoginFormDTO loginFormDTO) {
        //1.校验手机号格式
        String phone = loginFormDTO.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误！");
        }
        User user = query().eq("phone", phone).one();
        if(user != null){
            return Result.fail("手机号已注册！");
        }
        //2.校验密码格式
        String password = loginFormDTO.getPassword();
        if(StrUtil.isBlank(password)){
            return Result.fail("密码不能为空！");
        }
        if(password.length() < 6){
            return Result.fail("密码长度不能小于6位！");
        }
        //3.创建新用户
        user = createUserWithPhone(phone,password);
        //4.返回注册成功
        return Result.ok("注册成功");
    }

    @Override
    public Map<String, String> createCaptcha() {
        // 定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        // 获取验证码的文本内容
        String captchaText = lineCaptcha.getCode();
        log.info("验证码文本：{}", captchaText);
        // 生成唯一ID作为key
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String redisKey = "captcha:" + captchaId;
        // 将验证码文本内容保存到redis中
        stringRedisTemplate.opsForValue().set(redisKey, captchaText, 60, TimeUnit.SECONDS);
        
        // 将图片转为base64返回
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lineCaptcha.write(baos);
        // 拼接成完整的Data URL，完整结果："data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
        String base64Image = "data:image/png;base64," + 
            Base64.getEncoder().encodeToString(baos.toByteArray());
        
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaImage", base64Image);
        return result;
    }

    @Override
    public Result<String> verifyCaptcha(LoginFormDTO loginFormDTO) {
        // TODO getCaptchaId（）前端帮我传回
        String redisKey = "captcha:" + loginFormDTO.getCaptchaId();
        String correctCaptcha = stringRedisTemplate.opsForValue().get(redisKey);//正确验证码
        
        // 验证
        if (correctCaptcha == null) {
            return Result.fail("验证码已过期");
        }
        
        if (!correctCaptcha.equalsIgnoreCase(loginFormDTO.getCaptcha())) {
            return Result.fail("验证码错误");
        }
        // 验证成功，删除验证码
        stringRedisTemplate.delete(redisKey);

        return Result.ok("验证成功");
    }

    @Override
    public Result<String> logout(String authHeader) {
        
        // 1. 提取 token
        String token = extractToken(authHeader);
        if (StringUtils.isBlank(token)) {
            return Result.fail("未提供有效的认证信息");
        }
        
        // 2. 验证 token 格式
        if (!isValidTokenFormat(token)) {
            return Result.fail("token格式错误");
        }
        
        // 3. 从 Redis 删除 token
        String redisKey = "token:" + token;
        boolean deleted = Boolean.TRUE.equals(stringRedisTemplate.delete(redisKey));
        
        if (deleted) {
            log.info("用户退出成功，token: {}", token);
            return Result.ok("退出成功");
        } else {
            log.warn("token已失效: {}", token);
            return Result.fail("token已失效");
        }
    }
    
    private String extractToken(String authHeader) {
        if (StringUtils.isBlank(authHeader)) {
            return null;
        }
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }

    private boolean isValidTokenFormat(String token) {
        // 简单验证：token长度、格式等
        return StringUtils.isNotBlank(token) && token.length() >= 20;
    }

    @Override
    public Result<String> updateUserInfo(User user) {
        // 1. 检查用户是否存在
        if (user.getId() == null) {
            return Result.fail("用户ID不能为空");
        }
        // 2. 更新用户信息
        userMapper.updateById(user);
        return Result.ok("更新成功");
    }





}