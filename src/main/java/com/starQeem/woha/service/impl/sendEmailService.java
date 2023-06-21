package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starQeem.woha.config.email;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.userService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import static com.starQeem.woha.util.constant.EMAIL_FORM;

/**
 * @Date: 2023/6/21 12:01
 * @author: Qeem
 */
@Service
public class sendEmailService {
    @Resource
    private userService userService;
    @Resource
    private email email;
    @Async
    public void sendEmail(comment comment, String title, Integer type) throws MessagingException {
        //回复的用户
        QueryWrapper<user> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.select("id","email").eq("id",comment.getCommentUserId());
        user user1 = userService.getBaseMapper().selectOne(queryWrapper1);
        //发布回复评论的用户
        QueryWrapper<user> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.select("id","nick_name").eq("id",comment.getUserId());
        user user2 = userService.getBaseMapper().selectOne(queryWrapper2);
        //发送提醒邮件
        email.sendVerificationCommentHint(EMAIL_FORM,user1.getEmail(),user2.getNickName(),comment,title,type);
    }
}
