package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Component
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String upload;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    //跳转到账户设置的页面
    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    //获取上传的图片名
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您未选择图片！");
            return "/site/setting";
        }
        //获取上传的文件名
        String filename = headerImage.getOriginalFilename();
        //找到文件名的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File disk = new File(upload + "/"+filename);
        try {
            //存储文件
            headerImage.transferTo(disk);
        }catch (IOException e){
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常！",e);
        }

        //更新当前用户的头像路径(web访问路径，因为图片存到了硬盘上，不能直接存硬盘地址！)
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/"+filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    //获取头像,由于输出的是图片，则用IO流输出
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = upload + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1)
                os.write(buffer,0,b);
        }catch (IOException e) {
            logger.error("读取头像失败:"+e.getMessage());
        }
    }

    //修改密码
    @LoginRequired
    @RequestMapping(path = "/ModifyPassword", method = RequestMethod.POST)
    public String register(Model model,String newpassword,String oldpassword,
                           @CookieValue("ticket") String code) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldpassword, newpassword);
        //map为空说明没有问题
        if (map == null || map.isEmpty()) {
            //退出用户，修改该凭证的相关信息
            userService.logout(code);
            model.addAttribute("msg", "密码修改成功，即将跳转到登录页面");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        } else {
            //map为空说明出现了问题，并转到register页面中
            model.addAttribute("pwdMsg", map.get("pwdMsg"));
            return "/site/setting";
        }
    }

    /**
     *
     * @Description: 个人简介
     * @param userId: 用户id
     * @param model: 实体model
     * @author Tong
     * @date 2020/06/29
     */
    @RequestMapping(path="/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null)
            throw new RuntimeException("该用户不存在!");

        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量(人)
        long followeeCount = followService.findFolloweeCount(user.getId(), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //判断是否进行了关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";


    }

}
