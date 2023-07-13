package com.starQeem.woha.controller.my;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.service.commentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import static com.starQeem.woha.util.constant.COMMENT_PAGE_SIZE;

/**
 * @Date: 2023/4/29 17:49
 * @author: Qeem
 */
@Controller
@RequestMapping("/my/comment")
public class MyCommentController {
    @Resource
    private commentService commentService;
    /*
    * 我的评论发布
    * */
    @PostMapping
    public String CommentInput(Comment comment) throws MessagingException {
        if (comment.getStoryId() == null && comment.getStrategyId() == null && comment.getPicturesId() == null){
            return null;
        }
        commentService.Comment(comment); //保存评论信息
        if (comment.getPicturesId() != null){
            return "redirect:/my/pictures/mypicturesdetail/"+comment.getPicturesId();
        }
        if (comment.getStoryId() != null){
            return "redirect:/my/story//mystory/storydetail/"+comment.getStoryId();
        }
        return "redirect:/my/strategy/strategydetail/"+comment.getStrategyId();
    }
    /*
    * 我的评论删除
    * */
    @RequestMapping("/delete/{commentId}")
    public String picturesCommentDelete(@PathVariable("commentId")Long id,
                                        @RequestParam(value = "picturesId", required = false) Long picturesId,
                                        @RequestParam(value = "storyId", required = false) Long storyId,
                                        @RequestParam(value = "strategyId",required = false)Long strategyId){
        if (picturesId == null && storyId == null && strategyId == null){
            return null;
        }
        commentService.removeComment(id); //根据id删除评论
        if (picturesId != null){
            return "redirect:/my/pictures/mypicturesdetail/"+picturesId;
        }
        if (storyId != null){
            return "redirect:/my/story/mystory/storydetail/"+storyId;
        }
        return "redirect:/my/strategy/strategydetail/"+strategyId;
    }
    /*
    * 回复我的评论
    * */
    @GetMapping(value = {"/info","/info/{pageNum}"})
    public String info(@PathVariable(value = "pageNum",required = false)Integer pageNum, Model model){
        PageInfo<Comment> pageInfo = commentService.info(pageNum,COMMENT_PAGE_SIZE); //查询回复我的评论列表
        model.addAttribute("page",pageInfo);
        return "my/info";
    }
    /*
    * 我的所有文章的评论
    * */
    @GetMapping(value = {"/comment","/comment/{pageNum}"})
    public String comment(@PathVariable(value = "pageNum",required = false)Integer pageNum,Model model){
        PageInfo<Comment> pageInfo = commentService.comment(pageNum,COMMENT_PAGE_SIZE);  //查询所有评论我的评论
        model.addAttribute("page",pageInfo);
        return "my/comment";
    }
}
