package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.jws.WebParam;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    //实例化记录日志的组件，每个类用自己的logger
    //Logger是一个接口，有5种级别打印
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        //map为空说明没有问题
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            //map为空说明出现了问题，并转到register页面中
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //激活链接：http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code")String code){
        int result = userService.activation(userId,code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功,您的账号可以正常使用了");
            model.addAttribute("target", "/login");
            return "/site/operate-result";
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已经激活过");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("msg", "激活失败,您的激活码不正确");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }
    }

    //访问验证码图片
    //验证码的图片是在进入login页面前，就进行了访问，因此在登录的时候会有一个cookie
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码相关的信息存入session
        //session.setAttribute("kaptcha",text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        //发给客户端,然后用户拿着这个cookie就知道相应的验证码了
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);


        //将图片输出给浏览器，通过流进行图片输出，因此返回void
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch (IOException e){
            logger.error("响应验证码失败:"+e.getMessage());
        }
    }

    //用户登录
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    //此时String username等参数不是像User类一样，这样Model不会自动注入这些参数
    //前台使用param来获得username，param代表从一次连接，然后从连接中获得参数
    public String login(String username, String password, String code,@RequestParam(defaultValue="false")Boolean rememberme
            , /*HttpSession session, */Model model, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
        //判断验证码是否为空/验证码是否正确
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(!code.equalsIgnoreCase(kaptcha) || StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //判断map中是否包含ticket,包含则表示登录成功
        if(map.containsKey("ticket")){
            //把ticket发送给客户端，通过Cookie
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //退出登录
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    //从众多的cookie中找到名为ticket的cookie
    public String logout(@CookieValue("ticket") String code){
        userService.logout(code);
        //清理认证
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
