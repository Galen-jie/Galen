package com.galen.seckill.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * JSON工具类
 *
 * @author Galen
 * @since 2024-01-01
 */
public class JsonUtil {

    /**
     * 对象转JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * JSON字符串转对象
     *
     * @param text  JSON字符串
     * @param clazz 对象类型
     * @return 对象
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * JSON字符串转List
     *
     * @param text  JSON字符串
     * @param clazz 元素类型
     * @return List
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * JSON字符串转JSONObject
     *
     * @param text JSON字符串
     * @return JSONObject
     */
    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    /**
     * JSON字符串转JSONArray
     *
     * @param text JSON字符串
     * @return JSONArray
     */
    public static JSONArray parseArray(String text) {
        return JSON.parseArray(text);
    }
}