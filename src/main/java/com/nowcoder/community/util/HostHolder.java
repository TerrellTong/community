package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<User>();

    //存入user
    public void setUser(User user){
        users.set(user);
    }

    //获得user
    public User getUser(){
        return users.get();
    }

    //清除user
    public void clearUser(){
        users.remove();
    }
}
