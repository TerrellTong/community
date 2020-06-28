package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    //注入Redis操作的工具
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *
     * @Description:  点赞功能
     * @Param:  userId：登录网站的用户
     *          entityType：点赞的类型(贴子/评论)
     *          entityId：点赞类型的id
     *          entityUserId：被点赞人的id
     * @return:
     * @author Tong
     * @date 2020/06/28
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //判断entityLikeKey中是否存在当前用户，即是否点赞了
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //开启事务，因为redis事务开启后不会执行相关的redis命令，因此isMember提前判断
                redisTemplate.multi();
                //如果存在当前用户，则把这个用户从点赞列表中移除
                //被点赞用户数-1
                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return redisTemplate.exec();
            }
        });
    }

    //查询某个实体（贴子/评论）的点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某个实体的点赞状态
    //为了以后业务扩展，有可能会出现踩，因此采用int为返回值
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        System.out.println(member);
        return member ? 1 : 0;
    }

    //查询某个用户的点赞数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }


}
