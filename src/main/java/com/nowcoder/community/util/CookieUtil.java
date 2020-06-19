package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getcookie(HttpServletRequest request,String name){
        if(name == null || request == null)
            throw new IllegalArgumentException("参数为空！");
        //通过request获得cookie数组
        Cookie[] coookies = request.getCookies();
        if(coookies != null){
            for(Cookie cookie:coookies){
                if(cookie.getName().equals(name))
                    return cookie.getValue();
            }
        }

        return null;
    }
}
