package com.galen.seckill.controller;

import com.galen.seckill.common.Result;
import com.galen.seckill.dto.UserLoginDTO;
import com.galen.seckill.dto.UserRegisterDTO;
import com.galen.seckill.dto.UserUpdateDTO;
import com.galen.seckill.entity.User;
import com.galen.seckill.service.UserService;
import com.galen.seckill.util.CookieUtil;
import com.galen.seckill.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Galen
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success(userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginDTO loginDTO,
                                 HttpServletResponse response) {
        String token = userService.login(loginDTO);
        CookieUtil.setCookie(response, "token", token, 1800);
        return Result.success(token);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserVO> updateUser(HttpServletRequest request,
                                       @Valid @RequestBody UserUpdateDTO updateDTO) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        UserVO userVO = userService.updateUser(userId, updateDTO);
        return Result.success(userVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtil.getCookieValue(request, "token");
        if (token != null) {
            CookieUtil.deleteCookie(response, "token");
        }
        return Result.success(null);
    }
}