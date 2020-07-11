package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //默认页面跳转
    @RequestMapping("/")
    public String index(Model model, HttpServletResponse response) {
        model.addAttribute("name", "simonsfan");
        //进行转发给/index
        return "forward:/index";
    }

    @RequestMapping(path="/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name="orderMode",defaultValue = "0") int orderMode){
        //在方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model.
        //所以，在thymeleaf中可以直接访问Page对象中的数据(相当于page已经存在了Model中了)
        page.setRows(discussPostService.findDiscussPostRows(0));
        //分页链接的请求/index?current=xxx ,current是存储在了page中
        page.setPath("/index?orderMode="+orderMode);

        //通过page类中的current的来计算offset，起始行
        List<DiscussPost> list = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList();
        if(list != null){
            for(DiscussPost post : list){
                Map<String,Object> map = new HashMap();
                map.put("post",post);
                //通过userService来通过UserId获取User
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                //贴子的赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("orderMode",orderMode);
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }
}
