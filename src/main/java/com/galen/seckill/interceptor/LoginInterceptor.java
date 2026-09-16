package com.galen.seckill.interceptor;

import com.galen.seckill.common.ResultCode;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录拦截器
 *
 * @author Galen
 * @since 2024-01-01
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Session中的用户信息
        User user = UserHolder.getUser();
        if (user == null) {
            log.warn("用户未登录，请求路径: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }

}