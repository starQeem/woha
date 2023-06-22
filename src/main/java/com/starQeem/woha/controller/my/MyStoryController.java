package com.starQeem.woha.controller.my;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.story;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.storyService;
import com.starQeem.woha.service.userTaskService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/18 9:54
 * @author: Qeem
 * 我的故事
 */
@Controller
@RequestMapping("my/story")
public class MyStoryController {
    @Resource
    private storyService storyService;
     /*
     * 查询故事
     * */
    @GetMapping(value = {"/mystory","/mystory/{pageNum}"})
    public String mystory(@PathVariable(value = "pageNum",required = false)Integer pageNum, Model model){
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        PageInfo<story> pageInfo = storyService.queryMyStory(pageNum, PAGE_SIZE,Long.valueOf(STATUS_ZERO));
        model.addAttribute("page",pageInfo);
        return "my/story";
    }
    /*
     * 跳转到我的故事发布页面
     * */
    @GetMapping("/mystoryInput")
    public String getmystoryInput(){
        return "my/storyInput";
    }
    /*
     * 我的故事发布
     * */
    @PostMapping("/mystoryInput")
    public String mystoryInput(story story){
        storyService.saveStory(story);
        return "redirect:/my/story/mystory";
    }
    /*
     * 跳转到我的故事编辑页面
     * */
    @GetMapping("/mystoryUpdate/{id}")
    public String getmystoryUpdate(@PathVariable("id")Long id, Model model){
        story story = storyService.getBaseMapper().selectById(id);
        model.addAttribute("story",story);
        return "my/storyUpdate";
    }
    /*
     * 故事编辑
     * */
    @PostMapping("/mystoryUpdate")
    public String mystoryUpdate(story story){
        storyService.updateStory(story);
        return "redirect:/my/story/mystory";
    }
    /*
     * 故事删除
     * */
    @RequestMapping("/mystoryDelete/{id}")
    public String mystoryDeleteById(@PathVariable("id")Long id, @RequestParam(value = "pageNum",required = false)Integer pageNum){
        storyService.removeStoryById(id);
        if (pageNum == null){
            return "redirect:/story";
        }
        return "redirect:/my/story/mystory/" + pageNum;
    }
    /*
     * 故事详情
     * */
    @GetMapping("/mystory/storydetail/{id}")
    public String mystoryById(@PathVariable("id")Long id, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        story story = storyService.queryStoryDetail(id);
        List<comment> commentList = storyService.getComments(id);
        Integer liked = storyService.getLikedCount(id);
        boolean status = storyService.getStatus(id);//查询是否点赞
        List<user> likedUserThree = storyService.getLikedUserThree(id);
        model.addAttribute("story",story);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/storydetail";
    }
}
