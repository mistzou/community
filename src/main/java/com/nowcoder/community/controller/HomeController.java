package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "index",method = RequestMethod.GET)
    public String index(Model model, Page page){
        page.setRows(discussPostService.getDiscussPostRows(0));
        page.setPath("index");
        List<DiscussPost> list = discussPostService.getDiscussPostList(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPostList = new ArrayList<>();
        for (DiscussPost post: list) {
            Map<String,Object> map = new HashMap<>();
            map.put("discussPost",post);
            map.put("user",userService.getUserById(post.getUserId()));
            discussPostList.add(map);
        }
        model.addAttribute("discussPostList",discussPostList);
        return "/index";
    }

}
