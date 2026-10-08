package com.galen.seckill.aspect;

import com.galen.seckill.annotation.RequireRole;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 角色权限切面
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@Aspect
@Component
public class RoleAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        // 获取当前登录用户
        User user = UserHolder.getUser();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 获取需要的角色
        int[] roles = requireRole.value();

        // 检查用户是否拥有所需角色
        boolean hasRole = Arrays.stream(roles)
                .anyMatch(role -> role == user.getRole());

        if (!hasRole) {
            log.warn("用户{}无权限访问，需要角色: {}, 当前角色: {}",
                    user.getId(), Arrays.toString(roles), user.getRole());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}