package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.util.Date;
import java.util.Random;

@Service("alphaService")
//默认单例，prototype是多例模式
/*@Scope("prototype")*/
public class AlphaService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    //REQUIRED  支持当前事务(外部事务)，如果不存在则创建新事务
    //REQUIRES_NEW 创建一个新事务，并暂停当前事务(外部事务)
    //NESTED  如果当前存在事务(外部事务)，则嵌套在该事务中执行(独立的提交和回滚)，否则和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public String save1(){
        User user = new User();
        //注册
        user.setUsername("HHHH");
        user.setPassword("1002");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        int count = userMapper.insertUser(user);

        //新用户默认发帖
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("新人报道");
        post.setContent("小小萌新一枚！请多多关照！");
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        Integer.valueOf("abc");
        return "ok";
    }

    public String save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus status) {
                User user = new User();
                //注册
                user.setUsername("ZLQ");
                user.setPassword("1002");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
                user.setType(0);
                user.setStatus(0);
                user.setActivationCode(CommunityUtil.generateUUID());
                user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
                user.setCreateTime(new Date());
                int count = userMapper.insertUser(user);

                //新用户默认发帖
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("新人报道");
                post.setContent("小小萌新一枚！请多多关照！");
                post.setCreateTime(new Date());
                discussPostService.addDiscussPost(post);

                Integer.valueOf("abc");
                return "ok";
            }
        });
    }

}
