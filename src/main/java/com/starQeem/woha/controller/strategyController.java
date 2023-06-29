package com.starQeem.woha.controller;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.strategyService;
import com.starQeem.woha.service.strategyTypeService;
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
 * @Date: 2023/4/22 22:12
 * @author: Qeem
 * 攻略
 */
@Controller
@RequestMapping("/strategy")
public class strategyController {
    @Resource
    private strategyTypeService strategyTypeService;
    @Resource
    private strategyService strategyService;
    /*
    * 查询攻略列表
    * */
    @GetMapping(value = {"","/{id}"})
    public String strategy(@PathVariable(value = "id",required = false)Long id,
                          @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                          Model model, String title){
        List<strategyType> typeList = strategyTypeService.queryStrategyType();  //查询所有分类
        model.addAttribute("typeList",typeList);
        if (id == null){
            id = typeList.get(0).getId();
        }
        PageInfo<strategy> pageInfo = strategyService.pageStrategyWithStrategyTypeById(pageNum, PAGE_SIZE, id, title);  //查询分类列表
        model.addAttribute("page",pageInfo);
        model.addAttribute("currType",id);
        return "strategy";
    }
    /*
    * 查询用户发布的攻略列表
    * */
    @GetMapping(value = {"/user/{id}","/user/{id}/{pageNum}"})
    public String UserStrategy(@RequestParam(value = "pageNum",required = false)Integer pageNum,
                               @PathVariable("id") Long id,Model model){
        PageInfo<strategy> pageInfo = strategyService.getUserWithStrategyWithStrategyType(pageNum, PAGE_SIZE, id);
        model.addAttribute("page",pageInfo);
        model.addAttribute("userStrategy","1");
        model.addAttribute("userId",id);
        return "strategy";
    }
    /*
    * 查询攻略详情
    * */
    @GetMapping("strategydetail/{id}")
    public String strategyDetail(@PathVariable("id")Long id, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            //已登录
            strategy strategy = strategyService.queryStrategyDetailById(id,Long.valueOf(user.getId()));//查询文章详情包含任务相关的业务
            boolean status = strategyService.getStatus(id);//查询是否点赞
            model.addAttribute("strategy", strategy);
            model.addAttribute("status",status);
            model.addAttribute("userId",Long.valueOf(user.getId())); //userId（这个是传给前端做评论区有没有点赞判断的）
        }else {
            //未登陆
            strategy strategy = strategyService.getStrategyDetailById(id);//查询文章详情不包含任务相关的业务
            model.addAttribute("message","登录后才能发布评论哦!");
            model.addAttribute("strategy", strategy);
            model.addAttribute("status",false);//默认未点赞
            model.addAttribute("likeMessage","喜欢吗?登录为楼主点个赞吧~");
            model.addAttribute("userId",0); //未登陆时传个0过去（这个是传给前端做评论区有没有点赞判断的）
        }
        Integer liked = strategyService.getLikedCount(id);  //点赞数
        List<comment> commentList = strategyService.getComments(id);  //评论区
        List<user> likedUserThree = strategyService.getLikedUserThree(id);  //点赞排行榜
        model.addAttribute("commentsList", commentList);
        model.addAttribute("liked", liked);
        model.addAttribute("likedUserThree", likedUserThree);
        return "strategydetail";
    }
    /*
     * 点赞
     * */
    @PostMapping("/liked/{id}")
    public String liked(@PathVariable("id")Long id){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            strategyService.liked(id, Long.valueOf(user.getId()));
        }
        return "redirect:/strategy/strategydetail/" + id;
    }
}
