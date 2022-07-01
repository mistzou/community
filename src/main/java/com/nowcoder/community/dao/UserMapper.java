package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User getUserByid(int id);

    int insertUser(User user);

    int updateUserByid(int id,String username);

    int delteUserByid(int id);

}
