package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//只处理Controller注解的异常
//由于SpringBoot只是进行了页面的跳转，但是没有记录相应的日志信息
//因此用@ControllerAdvice进行，异常处理
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //处理所有异常的方法，对500异常进行相应的处理
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常:" + e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        String xRequestedWith = request.getHeader("x-requested-with");
        //如果是异步请求，发送字符串
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        }else {
            //非异步请求，直接重定向到error 500页面
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }
}
