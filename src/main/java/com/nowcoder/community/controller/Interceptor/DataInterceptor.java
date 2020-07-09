package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DateService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DateService dateService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //由于每次发请求都要进行统计，因此写入拦截器中
        //记录UV
        //获取请求的ip
        String ip = request.getRemoteHost();
        dateService.recordUV(ip);

        //记录DAU
        User user = hostHolder.getUser();
        if(user != null)
            dateService.recordDAU(user.getId());

        return true;
    }
}
