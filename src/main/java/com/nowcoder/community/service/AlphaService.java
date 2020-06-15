package com.nowcoder.community.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {
    public AlphaService(){
        System.out.println("实例化");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化");
    }
    @PreDestroy
    public void destory(){
        System.out.println("销毁");
    }

}
