package com.galen.seckill.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.galen.seckill.entity.User;
import com.galen.seckill.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class RefreshInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    RefreshInterceptor(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if(token == null){
            return true;
        }
        Map<Object,Object> userMap = redisTemplate.opsForHash().entries("galen:token:"+token);
        if(userMap.isEmpty()) {
            return true;
        }
        User user = BeanUtil.fillBeanWithMap(userMap, new User(), false);
        UserHolder.setUser(user);
        redisTemplate.expire("galen:token:" + token, 30, TimeUnit.MINUTES);
        return true;
    }
}
