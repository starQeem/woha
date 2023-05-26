package com.starQeem.woha.controller;

import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.service.commentService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

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
    public String commentInput(comment comment) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            commentService.Comment(comment);
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
    * 点赞
    * */
    @PostMapping("/liked/{commentId}")
    public String commentLiked(@PathVariable("commentId")Long commentId, comment comment, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
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
