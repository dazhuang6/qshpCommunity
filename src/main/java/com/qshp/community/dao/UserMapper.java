package com.qshp.community.dao;

import com.qshp.community.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("select * from user where id = #{id}")
    User selectById(int id);

    @Select("select * from user where username = #{username}")
    User selectByName(String username);

    @Select("select * from user where email = #{email}")
    User selectByEmail(String email);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert("insert into user(username, password, salt, email, type, status, activation_code, header_url, create_time) " +
            "values(#{username}, #{password}, #{salt}, #{email}, #{type}, #{status}, #{activationCode}, #{headerUrl}, #{createTime})")
    int insertUser(User user);

    @Update("update user set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);

    @Update("update user set header_url = #{headerUrl} where id = #{id}")
    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    @Update("update user set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") int id, @Param("password") String password);

}
