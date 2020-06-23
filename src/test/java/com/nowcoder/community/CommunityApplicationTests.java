package com.nowcoder.community;


import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    //通过重写这个构造方法，可以获取Spring容器，
    // ApplicationContext就是一个Spring容器，ApplicationContext的最高父类是BeanFactory
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取Spring容器
        this.applicationContext = applicationContext;
    }

    @Test
    public void testBean(){
        //通过Spring容器获取Bean对象
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());
    }

    @Test
    public void testmanage(){
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    @Test
    public void testConfig(){
        SimpleDateFormat simpleDateFormat = applicationContext.getBean("simpleDateFormat",SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    //常用的bean调用方法
    @Autowired
    private AlphaDao alphaDao;

    @Test
    public void testDi(){
        System.out.println(alphaDao.select());
    }
}
