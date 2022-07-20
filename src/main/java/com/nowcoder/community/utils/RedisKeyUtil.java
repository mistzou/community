package com.nowcoder.community.utils;

public class RedisKeyUtil {

    public final static String SPLIT = ":";

    public final static String PREFIX_ENTITY_LIKE = "like:entity";

    public final static String PREFIX_USER_LIKE = "like:user";

    public final static String PREFIX_USER_SET_LIKE = "like:user:set";


    public final static String PREFIX_FOLLOWEE = "followee";

    public final static String PREFIX_FOLLOWER = "follower";

    public final static String PREFIX_KAPTCHA = "kaptcha";

    public final static String PREFIX_TICKET = "ticket";

    public final static String PREFIX_USER = "user";


    //某个实体的赞
    public static String getEntityKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    //某个用户收到的赞
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    public static String getUserSetLikeKey(int userId){
        return PREFIX_USER_SET_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的粉丝
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码key
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
}
