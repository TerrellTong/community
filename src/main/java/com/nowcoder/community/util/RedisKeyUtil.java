package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    //构造redis的公共前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";


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
}
