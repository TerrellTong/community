package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //按照entityType进行comment查找，并按offset，limit进行分页
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    //根据entitytype计算总评论数
    int selectCountByEntity(int entityType,int entityId);

    //插入贴子
    int insertComment(Comment comment);

    Comment findCommentById(int id);
}
