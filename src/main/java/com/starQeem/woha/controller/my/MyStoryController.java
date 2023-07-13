package com.starQeem.woha.controller.my;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.pojo.Story;
import com.starQeem.woha.pojo.User;
import com.starQeem.woha.service.storyService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
     * 查询问答
     * */
    @GetMapping(value = {"/mystory","/mystory/{pageNum}"})
    public String myStory(@PathVariable(value = "pageNum",required = false)Integer pageNum, Model model){
        PageInfo<Story> pageInfo = storyService.queryMyStory(pageNum, PAGE_SIZE, (long) STATUS_ZERO); //查询问答列表
        model.addAttribute("page",pageInfo);
        return "my/story";
    }
    /*
     * 跳转到我的问答发布页面
     * */
    @GetMapping("/mystoryInput")
    public String getMyStoryInput(){
        return "my/storyInput";
    }
    /*
     * 我的故事发布
     * */
    @PostMapping("/mystoryInput")
    public String myStoryInput(Story story){
        storyService.saveStory(story); //问答发布
        return "redirect:/my/story/mystory";
    }
    /*
     * 跳转到我的问答编辑页面
     * */
    @GetMapping("/mystoryUpdate/{id}")
    public String getMyStoryUpdate(@PathVariable("id")Long id, Model model){
        Story story = storyService.getBaseMapper().selectById(id); //根据问答id查询问答详情
        model.addAttribute("story",story);
        return "my/storyUpdate";
    }
    /*
     * 问答编辑
     * */
    @PostMapping("/mystoryUpdate")
    public String myStoryUpdate(Story story){
        storyService.updateStory(story);  //问答修改
        return "redirect:/my/story/mystory";
    }
    /*
     * 问答删除
     * */
    @RequestMapping("/mystoryDelete/{id}")
    public String myStoryDeleteById(@PathVariable("id")Long id, @RequestParam(value = "pageNum",required = false)Integer pageNum){
        storyService.removeStoryById(id); //根据问答id删除问答文章
        if (pageNum == null){
            return "redirect:/story";
        }
        return "redirect:/my/story/mystory/" + pageNum;
    }
    /*
     * 问答文章详情
     * */
    @GetMapping("/mystory/storydetail/{id}")
    public String myStoryById(@PathVariable("id")Long id, Model model){
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        Story story = storyService.queryStoryDetail(id);//查询问答详情
        List<Comment> commentList = storyService.getComments(id);//查询评论信息
        Integer liked = storyService.getLikedCount(id);//查询问答文章点赞数
        boolean status = storyService.getStatus(id);//查询是否点赞
        List<User> likedUserThree = storyService.getLikedUserThree(id); //查询问答文章点赞的前三名用户
        model.addAttribute("story",story);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/storydetail";
    }
}
