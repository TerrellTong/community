package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //根据用户id来获取贴子
    //当用户id为0的时候，直接返回首页
    //offset:项目的起始页，limit:每页显示多少数据
    List<DiscussPost>  selectDiscussPosts(int userId,int offset,int limit);

    //获取贴子（非拉黑）的数量,如果传入的userid为非0，则表示查询的是当前用户的发帖
    //@Param注解用于给参数取别名
    //当使用动态sql,if时，并且只有一个参数，则必须加别名
    int selectDiscussPostsRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

}
