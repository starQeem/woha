package com.starQeem.woha.controller;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.story;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.storyService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.starQeem.woha.util.constant.PAGE_NUM;
import static com.starQeem.woha.util.constant.PAGE_SIZE;

/**
 * @Date: 2023/4/21 12:27
 * @author: Qeem
 * 问答
 */
@Controller
@RequestMapping("/story")
public class storyController {
    @Resource
    private storyService storyService;
    /*
     * 查询问答列表
     * */
    @GetMapping(value = {"", "/{pageNum}"})
    public String story(@PathVariable(value = "pageNum", required = false) Integer pageNum, Model model, String title) {
        PageInfo<story> pageInfo = storyService.getStoryListPageInfo(pageNum, PAGE_SIZE, title); //查询问答列表
        model.addAttribute("page", pageInfo);
        return "story";
    }
    /*
    * 查询用户发布的问答列表
    * */
    @GetMapping(value = {"/user/{id}","/user/{id}/{pageNum}"})
    public String UserStory(@RequestParam(value = "pageNum",required = false)Integer pageNum,
                            @PathVariable("id") Long id,Model model){
        PageInfo<story> pageInfo = storyService.queryMyStory(pageNum, PAGE_SIZE, id); //根据用户id查询问答列表
        model.addAttribute("page",pageInfo);
        model.addAttribute("userStory","1");
        model.addAttribute("userId",id);
        return "story";
    }

    /*
     * 查询问答详情
     * */
    @GetMapping("/storydetail/{id}")
    public String storydetail(@PathVariable("id") Long id, Model model) {
        //获取当前登录的用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Integer liked = storyService.getLikedCount(id);  //获取点赞数
        List<user> likedUserThree = storyService.getLikedUserThree(id); //点赞排行榜
        if (user != null) {
            //已经登陆
            story story = storyService.queryStoryDetail(id);  //查询问答详情并且判断有没有完成每日任务，没有则把每日任务：观看一篇故事设置为已完成状态
            boolean status = storyService.getStatus(id);//查询是否点赞
            model.addAttribute("story", story);
            model.addAttribute("status",status);
            model.addAttribute("userId",Long.valueOf(user.getId()));
        } else {
            //未登陆
            story story = storyService.getStoryById(id);  //直接查文章详情，没有任务相关的业务
            model.addAttribute("message","登录后才可以发布评论哦!");
            model.addAttribute("story", story);
            model.addAttribute("status",false);//默认未点赞
            model.addAttribute("likeMessage","喜欢吗?登录为楼主点个赞吧~");
            model.addAttribute("userId",0);
        }
        List<comment> commentList = storyService.getComments(id);//查询评论区
        model.addAttribute("commentsList",commentList);
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "storydetail";
    }
    /*
    * 点赞
    * */
    @PostMapping("/liked/{id}")
    public String liked(@PathVariable("id")Long id){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            storyService.liked(id, Long.valueOf(user.getId()));
        }
        return "redirect:/story/storydetail/" + id;
    }
}
