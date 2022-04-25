package com.qshp.community.service.impl;

import com.qshp.community.dao.UserMapper;
import com.qshp.community.entity.User;
import com.qshp.community.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserMapper userMapper;

    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
}
