package com.starQeem.woha.controller;

import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.service.commentService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;

/**
 * @Date: 2023/5/1 11:43
 * @author: Qeem
 */
@Controller
@RequestMapping("/comment")
public class commentController {
    @Resource
    private commentService commentService;
    /*
     * 评论区发布
     * */
    @PostMapping
    public String commentInput(comment comment) throws MessagingException {
        if (comment.getPicturesId() == null && comment.getStrategyId() == null && comment.getStoryId() == null){
            return null;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            commentService.Comment(comment); //保存评论信息
        }
        if (comment.getPicturesId() != null) {
            return "redirect:/pictures/picturesdetail/" + comment.getPicturesId();
        }
        if (comment.getStoryId() != null){
            return "redirect:/story/storydetail/" + comment.getStoryId();
        }
        return "redirect:/strategy/strategydetail/" + comment.getStrategyId();
    }
    /*
    * 评论区点赞
    * */
    @PostMapping("/liked/{commentId}")
    public String commentLiked(@PathVariable("commentId")Long commentId, comment comment){
        if (comment.getPicturesId() == null && comment.getStrategyId() == null && comment.getStoryId() == null){
            return null;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            //保存点赞信息
            commentService.liked(commentId, Long.valueOf(user.getId()));
        }
        if (comment.getPicturesId()!=null){
            return "redirect:/pictures/picturesdetail/"+comment.getPicturesId();
        }
        if (comment.getStoryId()!=null){
            return "redirect:/story/storydetail/"+comment.getStoryId();
        }
        return "redirect:/strategy/strategydetail/"+comment.getStrategyId();
    }
}
