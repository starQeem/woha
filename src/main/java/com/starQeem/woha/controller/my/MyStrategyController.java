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
    * 查询我发布的攻略
    * */
    @GetMapping(value = {"/mystrategy","/mystrategy/{pageNum}"})
    public String strategy(@PathVariable(value = "pageNum",required = false)Integer pageNum,Model model){
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        PageInfo<strategy> pageInfo = strategyService.getUserWithStrategyWithStrategyType(pageNum, PAGE_SIZE,Long.valueOf(STATUS_ZERO));
        model.addAttribute("page",pageInfo);
        return "my/strategy";
    }
    /*
    * 跳转到百科发布页面
    * */
    @GetMapping("/strategyInput")
    public String getstrategyInput(Model model){
        List<strategyType> typeList = strategyTypeService.list();
        model.addAttribute("typeList",typeList);
        return "my/strategyInput";
    }
    /*
    * 百科发布
    * */
    @PostMapping("/strategyInput")
    public String strategyInput(strategy strategy){
        strategyService.saveStrategy(strategy);
        return "redirect:/my/strategy/mystrategy";
    }
    /*
    * 跳转到百科编辑页面
    * */
    @GetMapping("/strategyUpdate/{id}")
    public String getstrategyUpdate(@PathVariable("id") Long id,Model model){
        strategy strategy = strategyService.getUserWithStrategyWithStrategyTypeById(id);
        List<strategyType> typeList = strategyTypeService.list();
        model.addAttribute("strategy", strategy);
        model.addAttribute("typeList",typeList);
        return "my/strategyUpdate";
    }
    /*
    * 百科编辑
    * */
    @PostMapping("/strategyUpdate")
    public String strategyUpdate(strategy strategy){
        strategyService.updateStrategy(strategy);
        return "redirect:/my/strategy/mystrategy";
    }
    /*
    * 百科删除
    * */
    @RequestMapping("/strategyDelete/{id}")
    public String strategyDeleteById(@PathVariable("id")Long id, @RequestParam(value = "pageNum",required = false)Integer pageNum){
        strategyService.removeStrategyById(id);
        if (pageNum == null){
            return "redirect:/strategy";
        }
        return "redirect:/my/strategy/mystrategy/" + pageNum;
    }
    /*
    * 攻略详情
    * */
    @GetMapping("/strategydetail/{id}")
    public String strategyDetailById(@PathVariable("id")Long id,Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        strategy strategy = strategyService.queryStrategyDetailById(id,Long.valueOf(user.getId()));
        List<comment> commentList = strategyService.getComments(id);
        boolean status = strategyService.getStatus(id);//查询是否点赞
        Integer liked = strategyService.getLikedCount(id);
        List<user> likedUserThree = strategyService.getLikedUserThree(id);
        model.addAttribute("strategy", strategy);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/strategydetail";
    }
}
