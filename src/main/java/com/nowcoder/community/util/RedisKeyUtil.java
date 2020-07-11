package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    //构造redis的公共前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER= "follower";
    private static final String PREFIX_KAPTCHA= "kaptcha";
    private static final String PREFIX_TICKET= "ticket";
    private static final String PREFIX_USER= "user";
    private static final String PREFIX_UV= "uv";
    private static final String PREFIX_DAU= "dav";
    private static final String PREFIX_POST= "post";
    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId)
    //为了方便以后出现要知道点赞人的有哪些，因此用set集合
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //某个用户的赞
    //like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType -> zset(entityId,now)
    //followee:关注的人
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+userId+SPLIT+entityType;
    }

    //某个实体（贴子，人物）拥有的粉丝
    //follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    //登录验证码
    //用户登录前，发送一个临时凭证，用来标识某一个用户
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }

    //登录的凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }

    //单日UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    //区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT +endDate;
    }

    //帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
