package com.nowcoder.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    //通过UUID的方式生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    //通过md5加密password
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}