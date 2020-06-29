package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    //构造redis的公共前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER= "follower";

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
        return PREFIX_USER_LIKE+SPLIT+userId+SPLIT+entityType;
    }

    //某个实体（贴子，人物）拥有的粉丝
    //follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_USER_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
}
