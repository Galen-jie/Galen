package com.galen.seckill.util;

import com.galen.seckill.entity.User;

public class UserHolder{
    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User user){
        userThreadLocal.set(user);
    }
    public static User getUser(){
        return userThreadLocal.get();
    }
    public static void remove(){
        userThreadLocal.remove();
    }
}
