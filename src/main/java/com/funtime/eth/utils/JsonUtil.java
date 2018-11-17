package com.funtime.eth.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

public class JsonUtil {
    public static Map<String, Object> convertJsonStrToMap(String jsonStr) {

        Map<String, Object> map = JSON.parseObject(
                jsonStr, new TypeReference<Map<String, Object>>() {
                });

        return map;
    }

}
