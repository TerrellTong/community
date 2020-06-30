package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@Deprecated
public interface LoginTicketMapper {
    //插入一条LoginTicket
    int insertLoginTickt(LoginTicket loginTicket);

    //根据ticket查询LoginTicket
    LoginTicket selectByTicket(String ticket);

    //更新LoginTicket
    int updateStatus(String ticket,int status);

}
