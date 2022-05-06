package com.qshp.community.service;

import com.qshp.community.entity.LoginTicket;
import com.qshp.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    User findUserById(int id);
    Map<String, Object> register(User user);
    int activation(int userId, String code);
    Map<String, Object> login(String username, String password, int expiredSeconds);
    void logout(String ticket);
    LoginTicket findLoginTicket(String ticket);
    void updateHeader(int userId, String headerUrl);
    Map<String, Object> updatePassword(int userId, String oldPwd, String newPwd);
    User findUserByName(String username);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
