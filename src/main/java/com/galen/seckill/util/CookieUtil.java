package com.galen.seckill.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cookie工具类
 *
 * @author Galen
 * @since 2024-01-01
 */
public class CookieUtil {

    /**
     * 获取Cookie值
     *
     * @param request    请求
     * @param cookieName Cookie名称
     * @return Cookie值
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 设置Cookie
     *
     * @param response     响应
     * @param cookieName   Cookie名称
     * @param cookieValue  Cookie值
     * @param maxAge       过期时间（秒）
     */
    public static void setCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 删除Cookie
     *
     * @param response   响应
     * @param cookieName Cookie名称
     */
    public static void deleteCookie(HttpServletResponse response, String cookieName) {
        setCookie(response, cookieName, null, 0);
    }
}