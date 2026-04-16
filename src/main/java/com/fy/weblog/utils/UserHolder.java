package com.fy.weblog.utils;

import com.fy.weblog.dto.UserDTO;

//每个用户独立存储
public class UserHolder {
    //为每个线程提供一个独立的变量副ThreadLocal
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    //保存当前线程的用户信息
    public static void saveUser(UserDTO user){
        tl.set(user);
    }
    
    //获取当前线程的用户信息
    public static UserDTO getUser(){
        return tl.get();
    }

    //移除当前线程的用户信息    
    public static void removeUser(){
        tl.remove();
    }
}
