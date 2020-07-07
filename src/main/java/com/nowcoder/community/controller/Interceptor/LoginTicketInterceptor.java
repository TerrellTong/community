package com.nowcoder.community.controller.Interceptor;


import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //在访问前进行拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取名为ticket的cookie
        String ticket = CookieUtil.getcookie(request, "ticket");
        //如果此时的响应有ticket，说明已经登录了，没有则说明现在是未登录的状态
        if(ticket != null){
            //通过ticket获取LoginTicket
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //通过loginTicket的Userid获取user
                User user = userService.findUserById(loginTicket.getUserId());
                //防止多线程下出现并发问题，因此采用ThreadLocal进行User存储
                hostHolder.setUser(user);
                //由于登录有效，因此在此进行授权
                //构建用户认证的结果，并存入SecurityContext，以便Security进行授权
                //由于没有使用Security进行授权，因此要自己构建一个Authentication从而传给SecurityContextHolder
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    //响应处理后进行判断
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //从hostHolder中获得用户，如果不存在就不处理
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null)
            modelAndView.addObject("loginUser",user);
    }

    //TemplateEngine模板处理后进行以下操作
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clearUser();
        //清理认证
        SecurityContextHolder.clearContext();
    }
}
