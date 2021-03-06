package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService implements CommunityConstant {
    @Resource
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

 //   @Resource
//    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id){
     //   return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null)
            user = initCache(id);
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap();
        //空值处理
        if(user == null)
            throw new IllegalArgumentException("参数不能为空！");
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }

        //判断用户名是否重名
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","用户名已存在!");
            return map;
        }
        //判断邮箱是否已经被注册
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","邮箱已存在!");
            return map;
        }

        //添加用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        //String.format用于类型格式化重载，在字符串中添加参数
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活链接：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账户",content);

        return map;
    }

    //激活用户
    public int activation(int userid,String code){
        User user = userMapper.selectById(userid);
        //获取用户激活码
        if(user.getStatus() == 1)
            return ACTIVATION_REPEAT;
        else if(user.getActivationCode().equals(code)){
            //更新status
            userMapper.updateStatus(userid,1);
            clearCache(userid);
            return ACTIVATION_SUCCESS;
        }else
            return ACTIVATION_FAILURE;
    }

    //登录用户
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }

        //验证激活状态
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }

        //验证密码
        if(!user.getPassword().equals(CommunityUtil.md5(password+user.getSalt()))){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ expiredSeconds*1000));
    //    loginTicketMapper.insertLoginTickt(loginTicket);

        //将loginTicket存入redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //将对象写入时，如果不用set，则是把对象序列化然后进行了存储
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        //将登录凭证的ticket放入map中
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    //退出登录
    public void logout(String ticket){
    //    loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //退出就是改状态，登录状态存到了redis中
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    //通过ticket获得LoginTicket
    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更新用户图片
    public int updateHeader(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    //更新用户密码
    public Map<String,Object> updatePassword(User user,String oldpassword,String newpassword){
        Map<String,Object> map = new HashMap();
        String password = CommunityUtil.md5(oldpassword+user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("pwdMsg","原密码输入错误!");
            return map;
        }
        password = CommunityUtil.md5(newpassword+user.getSalt());
        userMapper.updatePassword(user.getId(),password);
        clearCache(user.getId());
        return null;

    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(redisKey);
        return  user;
    }

    //2.取不到时就初始化缓存
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user);
        return user;
    }

    //3.数据变更时清除用户缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //获取权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
