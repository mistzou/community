package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class mapperTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void selectTest(){
        User user = userMapper.getUserByid(1);
        System.out.println(user);
    }

    @Test
    public void insertTest(){
        User user = new User();
        user.setUsername("张三");
        user.setPassword("123456");
        user.setActivationCode("");
        user.setEmail("nowcoder1@sina.com");
        user.setCreateTime(new Date());
        user.setHeaderUrl("http://static.nowcoder.com/images/head/101.png");
        user.setSalt("49f10");
        user.setType(1);
        int count = userMapper.insertUser(user);
        System.out.println(count);
        System.out.println(user);
    }

    @Test
    public void updateTest(){
        int count = userMapper.updateUserByid(150,"李四");
        System.out.println(count);
    }

    @Test
    public void deleteTest(){
        int count = userMapper.delteUserByid(150);
        System.out.println(count);
    }


    @Test
    public void test(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10);
        for (DiscussPost post: list) {
            System.out.println(post);
        }
    }


}
