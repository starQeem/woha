package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.user;

import javax.mail.MessagingException;
import java.util.List;

/**
 * @Date: 2023/4/17 0:02
 * @author: Qeem
 */
public interface userService extends IService<user> {
    boolean saveRegister(String email, String password,String code);
    user queryMyMessage();
    user getMessage();
    void task();
    user getUserWithGrade(Long id);
    void sendCode(String email) throws MessagingException;
    user getAvatarAddress();
    boolean updatePassword(String username, String password, String newPassword);
    void frequent(String username);
}
