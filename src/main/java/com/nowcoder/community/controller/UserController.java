package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user/")
public class UserController implements CommunityConstant {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "setting",method = RequestMethod.GET)
    public String setting(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "upload",method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","????????????????????????");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","????????????????????????????????????");
            return "/site/setting";
        }

        //?????????????????????
        fileName = CommunityUtil.generateUUID()+suffix;
        //????????????????????????
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("?????????????????????"+e.getMessage());
            throw new RuntimeException("??????????????????,??????????????????"+e);
        }

        //???????????????????????????????????????(web??????)
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "header/{fileName}",method = RequestMethod.GET)
    public void header(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //??????????????????
        String filePath = uploadPath+"/"+fileName;
        //????????????
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //????????????
        response.setContentType("image/"+suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filePath);
                ){

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("?????????????????????"+e.getMessage());
        }
    }

    @RequestMapping(path = "updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassWord,String newPassWord,String confirmPassWord){
        if(oldPassWord == null){
            model.addAttribute("oldPassWordMsg","????????????????????????");
            return "/site/setting";
        }
        if(newPassWord == null){
            model.addAttribute("newPassWordMsg","????????????????????????");
            return "/site/setting";
        }
        if(confirmPassWord == null){
            model.addAttribute("confirmPassWordMsg","???????????????????????????");
            return "/site/setting";
        }
        if(!confirmPassWord.equals(newPassWord)){
            model.addAttribute("confirmPassWordMsg","??????????????????,??????????????????????????????");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if(!CommunityUtil.md5(oldPassWord+user.getSalt()).equals(user.getPassword())){
            model.addAttribute("oldPassWordMsg","?????????????????????");
            return "/site/setting";
        }
        if(CommunityUtil.md5(newPassWord+user.getSalt()).equals(user.getPassword())){
            model.addAttribute("newPassWordMsg","????????????????????????????????????");
            return "/site/setting";
        }
        newPassWord = CommunityUtil.md5(newPassWord+user.getSalt());
        userService.updateUserPassWord(user.getId(),newPassWord);
        return "redirect:/logout";
    }

    @RequestMapping("/profile/{userId}")
    public String getProfile(@PathVariable("userId")int userId,Model model){
        User user = userService.getUserById(userId);
        if(user == null){
            throw new RuntimeException("?????????????????????");
        }

        model.addAttribute("user",user);

        //??????????????????
        long likeCount = likeService.findUserLikeCount(userId);

        long setLikeCount = likeService.findUserSetLikeCount(userId);

        model.addAttribute("likeCount",likeCount);

        model.addAttribute("setLikeCount",setLikeCount);

        //???????????????????????????
        model.addAttribute("followeeCount",followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        //???????????????????????????
        model.addAttribute("followerCount",followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        //??????????????????????????????
        boolean followed = false;
        if(hostHolder.getUser() != null){
            followed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("followed",followed);
        return "/site/profile";
    }

}
