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
            model.addAttribute("error","您还未选择图片！");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","您上传的文件格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID()+suffix;
        //确定文件存放位置
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败！"+e.getMessage());
            throw new RuntimeException("上传文件失败,服务器异常！"+e);
        }

        //更新当前用户的头像访问路径(web访问)
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "header/{fileName}",method = RequestMethod.GET)
    public void header(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //文件存放路径
        String filePath = uploadPath+"/"+fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //响应图片
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
            logger.error("获取头像失败！"+e.getMessage());
        }
    }

    @RequestMapping(path = "updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassWord,String newPassWord,String confirmPassWord){
        if(oldPassWord == null){
            model.addAttribute("oldPassWordMsg","原密码不能为空！");
            return "/site/setting";
        }
        if(newPassWord == null){
            model.addAttribute("newPassWordMsg","新密码不能为空！");
            return "/site/setting";
        }
        if(confirmPassWord == null){
            model.addAttribute("confirmPassWordMsg","确认密码不能为空！");
            return "/site/setting";
        }
        if(!confirmPassWord.equals(newPassWord)){
            model.addAttribute("confirmPassWordMsg","两组密码不同,请确认是否输入正确！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if(!CommunityUtil.md5(oldPassWord+user.getSalt()).equals(user.getPassword())){
            model.addAttribute("oldPassWordMsg","原密码不正确！");
            return "/site/setting";
        }
        if(CommunityUtil.md5(newPassWord+user.getSalt()).equals(user.getPassword())){
            model.addAttribute("newPassWordMsg","新密码不能与旧密码相同！");
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
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user",user);

        //获得多少个赞
        long likeCount = likeService.findUserLikeCount(userId);

        long setLikeCount = likeService.findUserSetLikeCount(userId);

        model.addAttribute("likeCount",likeCount);

        model.addAttribute("setLikeCount",setLikeCount);

        //查询用户的关注数量
        model.addAttribute("followeeCount",followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        //查询用户的粉丝数量
        model.addAttribute("followerCount",followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        //判断该用户是否被关注
        boolean followed = false;
        if(hostHolder.getUser() != null){
            followed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("followed",followed);
        return "/site/profile";
    }

}
