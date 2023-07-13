package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.starQeem.woha.config.email;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.pojo.User;
import com.starQeem.woha.service.commentService;
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
    private commentService commentService;
    @Resource
    private email email;
    @Async
    public void sendEmail(Comment comment, String title, Integer type) throws MessagingException {
        //回复的用户
        User replyUser = userService.getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getEmail)
                .eq(User::getId,comment.getCommentUserId()));
        //发布回复评论的用户
        User newReplyUser = userService.getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getNickName)
                .eq(User::getId,comment.getUserId()));
        //回复的评论
        Comment commentInfo = commentService.getBaseMapper().selectOne(Wrappers.<Comment>lambdaQuery()
                .select(Comment::getId,Comment::getContent)
                .eq(Comment::getId,comment.getParentCommentId()));
        //发送提醒邮件
        email.sendVerificationCommentHint(EMAIL_FORM,replyUser.getEmail(),newReplyUser.getNickName(),comment,title,type,commentInfo.getContent());
    }
}
