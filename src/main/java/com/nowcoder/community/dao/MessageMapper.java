package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的所有会话列表
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话锁包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //插入私信
    int insertMessage(Message message);

    //修改消息的状态
    int updateStatus(List<Integer> ids,int status);

    //根据用户查询某个主题下最新的通知
    Message selectLatestNotice(int userId,String topic);

    //根据用户查询某个主题所包含的通知数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知的数量，如果topic为null，则查询所有的未读数量
    int selectNoticeUnreadCount(int userId,String topic);

    //根据某个用户，某个主题查询所包含的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}
