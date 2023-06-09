package com.starQeem.woha.config;

import com.starQeem.woha.pojo.Comment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/5/7 0:32
 * @author: Qeem
 */
@Component
public class email {
    @Resource
    private JavaMailSender javaMailSender;
    public void sendVerificationCode(String form,String to, String code) throws MessagingException {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(form);   //发送邮件的邮箱号
        simpleMailMessage.setTo(to);   //接收邮件的邮箱号
        simpleMailMessage.setText("[喔哈星] 验证码:"+code+"(验证码5分钟内有效)。请勿将验证码告诉他人哦!");   //邮件内容
        simpleMailMessage.setSubject("喔哈星");   //邮件标题
        javaMailSender.send(simpleMailMessage);  //发送
    }
    public void sendVerificationCommentHint(String form, String to, String nickName, Comment comment, String title, Integer type, String commentContent) throws MessagingException {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(form);   //发送邮件的邮箱号
        simpleMailMessage.setTo(to);   //接收邮件的邮箱号
        String suffix = "";
        if (type == TYPE_ONE){ //图片
            suffix = "Pictures/picturesdetail/" + comment.getPicturesId();
        }
        if (type == TYPE_TWO){ //问答
            suffix = "Story/storydetail/" + comment.getStoryId();
        }
        if (type == TYPE_THREE){ //文章
            suffix = "Strategy/strategydetail/" + comment.getStrategyId();
        }
        simpleMailMessage.setText("您在喔哈星的[" + title +"]中发布的评论:["+ commentContent +"]有新的回复,回复用户:[" + nickName + "] 回复信息:[" + comment.getContent() +"] 前去查看: wohaqeem.top/" + suffix);   //邮件内容
        simpleMailMessage.setSubject("喔哈星");   //邮件标题
        javaMailSender.send(simpleMailMessage);  //发送
    }
}
