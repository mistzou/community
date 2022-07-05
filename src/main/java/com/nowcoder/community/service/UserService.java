package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper LoginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User getUserById(int userId){
        return userMapper.getUserByid(userId);
    }

    //注册
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("emailMsg","email不能为空！");
            return map;
        }
        //验证账户是否存在
        User u = userMapper.getUserByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","用户名已存在！");
            return map;
        }
        u = userMapper.getUserByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该用户邮箱已存在！");
            return map;
        }
        //注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        int count = userMapper.insertUser(user);
        if(count>0){
            //发送激活邮件
            Context context = new Context();
            context.setVariable("email",user.getEmail());
            String url = domain + contextPath + "/activation/"+user.getId() + "/"+user.getActivationCode();
            context.setVariable("url",url);
            String content = templateEngine.process("/mail/activation",context);
            mailClient.sendMail(user.getEmail(),"激活邮件",content);
        }else{
            map.put("registerMsg","注册失败,请稍后重试！");
            return map;
        }

        return map;
    }


    //激活账号
    public int activationUtil(int userId,String code){
        User user = userMapper.getUserByid(userId);
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateUserStatus(userId,1);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else{
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String passwrd,int expiredseconds){
        Map<String,Object> map = new HashMap<>();

        //判断用户名是否为空
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }

        //判断密码是否为空
        if(StringUtils.isBlank(passwrd)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.getUserByName(username);
        if(user == null){
            map.put("usernameMsg","该用户不存在！");
            return map;
        }

        if(user.getStatus()==0){
            map.put("usernameMsg","该用户还未激活！");
            return map;
        }

        if(!user.getPassword().equals(CommunityUtil.md5(passwrd+user.getSalt()))){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredseconds*1000));
        LoginTicketMapper.insertLoginTicked(loginTicket);
        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    public int logout(String ticket,int status){
        return LoginTicketMapper.updateLoginTicked(ticket,status);
    }


    public LoginTicket findLoginTicket(String ticket){
        return LoginTicketMapper.selectLoginTickedByTicked(ticket);
    }

    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateUserHeader(userId,headerUrl);
    }

    public int updateUserPassWord(int userId,String password){
        return userMapper.updateUserPassWord(userId,password);
    }

}
