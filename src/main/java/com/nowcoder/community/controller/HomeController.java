package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String index(Model model, Page page){
        page.setRows(discussPostService.getDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.getDiscussPostList(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPostList = new ArrayList<>();
        for (DiscussPost post: list) {
            Map<String,Object> map = new HashMap<>();
            map.put("discussPost",post);
            map.put("user",userService.getUserById(post.getUserId()));

            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
            map.put("likeCount",likeCount);

            discussPostList.add(map);
        }
        model.addAttribute("discussPostList",discussPostList);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getError(){
        return "/error/500";
    }


}
