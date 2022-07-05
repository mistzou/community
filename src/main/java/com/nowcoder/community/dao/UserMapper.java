package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User getUserByid(int id);

    User getUserByName(String username);

    User getUserByEmail(String email);

    int insertUser(User user);

    int updateUserByid(int id,String username);

    int updateUserStatus(int id,int status);

    int updateUserHeader(int id,String headerUrl);

    int updateUserPassWord(int id,String password);

    int delteUserByid(int id);

}
