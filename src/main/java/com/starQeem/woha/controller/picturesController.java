package com.starQeem.woha.controller;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.picturesService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

import static com.starQeem.woha.util.constant.PAGE_NUM;
import static com.starQeem.woha.util.constant.PICTURES_PAGE_SIZE;


/**
 * @Date: 2023/4/21 17:04
 * @author: Qeem
 * 精美图片
 */
@Controller
@RequestMapping("/pictures")
public class picturesController {
    @Resource
    private picturesService picturesService;
    /*
    * 查询图片列表
    * */
    @GetMapping(value = {"","/{pageNum}"})
    public String pictures(@PathVariable(value = "pageNum",required = false)Integer pageNum, String title, Model model){
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        if (title == null){
            title = "";
        }
        PageInfo<pictures> pageInfo = picturesService.getPicturesListPageInfo(pageNum, PICTURES_PAGE_SIZE, title);
        model.addAttribute("page",pageInfo);
        return "picture";
    }
    /*
    * 查询图片详情
    * */
    @GetMapping("/picturesdetail/{id}")
    public String picturesdetail(@PathVariable("id")Long id, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Integer liked = picturesService.getLikedCount(id);  //点赞数
        pictures pictures = picturesService.getPicturesDetailById(id);  //图片详情
        List<comment> commentList = picturesService.getComments(id);  //评论区
        List<user> likedUserThree = picturesService.getLikedUserThree(id);  //点赞排行榜
        if (user == null){
            model.addAttribute("likeMessage","喜欢吗?登录为楼主点个赞吧~");
            model.addAttribute("message","登录后才可以发布评论哦!");
            model.addAttribute("status",false);//默认未点赞
            model.addAttribute("userId",0);
        }else {
            boolean status = picturesService.getStatus(id, Long.valueOf(user.getId()));//查询是否点赞
            model.addAttribute("status",status);
            model.addAttribute("userId",Long.valueOf(user.getId()));
        }
        model.addAttribute("liked",liked);
        model.addAttribute("pictures",pictures);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("likedUserThree",likedUserThree);
        return "picturesdetail";
    }
    /*
    * 点赞
    * */
    @PostMapping("/liked/{id}")
    public String liked(@PathVariable("id")Long id){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            boolean status = picturesService.liked(id, Long.valueOf(user.getId()));
        }
        return "redirect:/pictures/picturesdetail/" + id;
    }
}
