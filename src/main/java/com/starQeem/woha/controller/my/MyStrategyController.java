package com.starQeem.woha.controller.my;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.strategy;
import com.starQeem.woha.pojo.strategyType;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.strategyService;
import com.starQeem.woha.service.strategyTypeService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/22 15:10
 * @author: Qeem
 */
@Controller
@RequestMapping("my/strategy")
public class MyStrategyController {
    @Resource
    private strategyTypeService strategyTypeService;
    @Resource
    private strategyService strategyService;
    /*
    * 查询我发布的文章
    * */
    @GetMapping(value = {"/mystrategy","/mystrategy/{pageNum}"})
    public String strategy(@PathVariable(value = "pageNum",required = false)Integer pageNum,Model model){
        PageInfo<strategy> pageInfo = strategyService.getUserWithStrategyWithStrategyType(pageNum, PAGE_SIZE, (long) STATUS_ZERO);//查询文章列表
        model.addAttribute("page",pageInfo);
        return "my/strategy";
    }
    /*
    * 跳转到文章发布页面
    * */
    @GetMapping("/strategyInput")
    public String getStrategyInput(Model model){
        List<strategyType> typeList = strategyTypeService.list(); //查询所有文章分类
        model.addAttribute("typeList",typeList);
        return "my/strategyInput";
    }
    /*
    * 文章发布
    * */
    @PostMapping("/strategyInput")
    public String strategyInput(strategy strategy){
        strategyService.saveStrategy(strategy); //保存文章信息
        return "redirect:/my/strategy/mystrategy";
    }
    /*
    * 跳转到文章编辑页面
    * */
    @GetMapping("/strategyUpdate/{id}")
    public String getStrategyUpdate(@PathVariable("id") Long id,Model model){
        strategy strategy = strategyService.getUserWithStrategyWithStrategyTypeById(id);//根据文章id查询文章详情
        List<strategyType> typeList = strategyTypeService.list();//查询所有文章分类
        model.addAttribute("strategy", strategy);
        model.addAttribute("typeList",typeList);
        return "my/strategyUpdate";
    }
    /*
    * 文章编辑
    * */
    @PostMapping("/strategyUpdate")
    public String strategyUpdate(strategy strategy){
        strategyService.updateStrategy(strategy); //更新文章
        return "redirect:/my/strategy/mystrategy";
    }
    /*
    * 文章删除
    * */
    @RequestMapping("/strategyDelete/{id}")
    public String strategyDeleteById(@PathVariable("id")Long id, @RequestParam(value = "pageNum",required = false)Integer pageNum){
        strategyService.removeStrategyById(id); //根据文章id删除文章
        if (pageNum == null){
            return "redirect:/strategy";
        }
        return "redirect:/my/strategy/mystrategy/" + pageNum;
    }
    /*
    * 文章详情
    * */
    @GetMapping("/strategydetail/{id}")
    public String strategyDetailById(@PathVariable("id")Long id,Model model){
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        strategy strategy = strategyService.queryStrategyDetailById(id,Long.valueOf(user.getId())); //查询文章详情
        List<comment> commentList = strategyService.getComments(id); //查询评论信息
        boolean status = strategyService.getStatus(id);//查询是否点赞
        Integer liked = strategyService.getLikedCount(id); //查询文章点赞数
        List<user> likedUserThree = strategyService.getLikedUserThree(id); //查询点赞文章的前三名用户
        model.addAttribute("strategy", strategy);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/strategydetail";
    }
}
