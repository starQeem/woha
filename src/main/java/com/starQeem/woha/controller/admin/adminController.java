package com.starQeem.woha.controller.admin;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.strategyType;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.strategyTypeService;
import com.starQeem.woha.service.userService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @Date: 2023/6/22 20:52
 * @author: Qeem
 */
@RequestMapping("/admin")
@Controller
public class adminController {
    @Resource
    private userService userService;
    @Resource
    private strategyTypeService strategyTypeService;

    /**
     * 跳转到后台管理页面
     *
     * @param model 模型
     * @return {@link String}
     */
    @GetMapping
    public String admin(Model model){
        List<user> userList =userService.list();
        model.addAttribute("userList",userList);
        return "admin/admin";
    }

    /**
     * 更新用户状态
     *
     * @param id         id
     * @return {@link String}
     */
    @GetMapping("/status/{id}")
    public String updateStatus(@PathVariable("id")Long id){
        userService.updateStatus(id);
        return "redirect:/admin";
    }

    /**
     * 跳转到攻略类型管理页面
     *
     * @return {@link String}
     */
    @GetMapping(value = {"/strategyType/{pageNum}","/strategyType"})
    public String strategyType(@PathVariable(value = "pageNum",required = false)Integer pageNum,Model model){
        PageInfo<strategyType> pageInfo = strategyTypeService.getStrategyTypeList(pageNum);
        model.addAttribute("page",pageInfo);
        return "admin/strategyType";
    }

    /**
     * 跳转到分类新增页面
     *
     * @return {@link String}
     */
    @GetMapping("/strategyTypeInput")
    public String getStrategyTypeInput(){
        return "admin/strategyTypeInput";
    }

    /**
     * 分类新增
     *
     * @param strategyType 策略类型
     * @return {@link String}
     */
    @PostMapping("/strategyTypeInput")
    public String StrategyTypeInput(strategyType strategyType){
        strategyTypeService.saveStrategyType(strategyType);
        return "redirect:/admin/strategyType";
    }

    /**
     * 跳转到攻略分类修改页面
     *
     * @return {@link String}
     */
    @GetMapping("/strategyType/{id}/edit")
    public String getStrategyTypeUpdate(@PathVariable("id")Long id,Model model){
        strategyType strategyType = strategyTypeService.getStrategyTypeById(id);
        model.addAttribute("strategyType",strategyType);
        return "admin/strategyTypeUpdate";
    }

    /**
     * 攻略分类修改
     *
     * @param strategyType 策略类型
     * @return {@link String}
     */
    @PostMapping("/strategyTypeUpdate")
    public String StrategyTypeUpdate(strategyType strategyType){
        strategyTypeService.StrategyTypeUpdateById(strategyType);
        return "redirect:/admin/strategyType";
    }

    /**
     * 策略类型删除
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping("/strategyType/{id}/delete")
    public String StrategyTypeDelete(@PathVariable("id")Long id,@RequestParam("pageNum")Integer pageNum){
        strategyTypeService.StrategyTypeDeleteById(id);
        return "redirect:/admin/strategyType/" + pageNum;
    }
}
