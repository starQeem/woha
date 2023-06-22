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
    /**
     * 用户注册
     *
     * @param email    邮箱地址
     * @param password 密码
     * @param code     验证码
     * @return boolean
     */
    boolean saveRegister(String email, String password);

    /**
     * 查询我信息
     *
     * @return {@link user}
     */
    user queryMyMessage();

    /**
     * 我的信息回显
     *
     * @return {@link user}
     */
    user getMessage();

    /**
     * 任务
     */
    void task();

    /**
     * 获取用户信息
     *
     * @param id 用户id
     * @return {@link user}
     */
    user getUserWithGrade(Long id);

    /**
     * 发送验证码
     *
     * @param email 邮箱地址
     * @throws MessagingException 通讯异常
     */
    void sendCode(String email) throws MessagingException;

    /**
     * 获取用户头像地址
     *
     * @return {@link user}
     */
    user getAvatarAddress();

    /**
     * 修改密码
     *
     * @param username    用户名
     * @param password    密码
     * @param newPassword 新密码
     * @return boolean
     */
    boolean updatePassword(String username, String password, String newPassword);

    /**
     * 更新状态
     *
     * @param id id
     */
    void updateStatus(Long id);

    /**
     * 得到用户状态
     *
     * @param username 用户名
     * @return boolean
     */
    boolean getUserStatus(String username);
}
