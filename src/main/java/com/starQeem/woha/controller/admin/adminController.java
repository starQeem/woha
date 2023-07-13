package com.starQeem.woha.controller.admin;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.StrategyType;
import com.starQeem.woha.pojo.User;
import com.starQeem.woha.service.strategyTypeService;
import com.starQeem.woha.service.userService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


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
    @GetMapping(value = {"","/{pageNum}"})
    public String admin(Model model,@PathVariable(value = "pageNum",required = false)Integer pageNum,String nickName){
        PageInfo<User> pageInfo =userService.getUserList(pageNum,nickName);//获取用户列表
        model.addAttribute("page",pageInfo);
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
        userService.updateStatus(id); //更新用户状态
        return "redirect:/admin";
    }

    /**
     * 跳转到文章类型管理页面
     *
     * @return {@link String}
     */
    @GetMapping(value = {"/strategyType/{pageNum}","/strategyType"})
    public String strategyType(@PathVariable(value = "pageNum",required = false)Integer pageNum,Model model){
        PageInfo<StrategyType> pageInfo = strategyTypeService.getStrategyTypeList(pageNum); //获取文章类型列表
        model.addAttribute("page",pageInfo);
        return "admin/strategyType";
    }

    /**
     * 跳转到文章分类新增页面
     *
     * @return {@link String}
     */
    @GetMapping("/strategyTypeInput")
    public String getStrategyTypeInput(){
        return "admin/strategyTypeInput";
    }

    /**
     * 文章分类新增
     *
     * @param strategyType 策略类型
     * @return {@link String}
     */
    @PostMapping("/strategyTypeInput")
    public String StrategyTypeInput(StrategyType strategyType){
        strategyTypeService.saveStrategyType(strategyType); //保存文章分类
        return "redirect:/admin/strategyType";
    }

    /**
     * 跳转到文章分类修改页面
     *
     * @return {@link String}
     */
    @GetMapping("/strategyType/{id}/edit")
    public String getStrategyTypeUpdate(@PathVariable("id")Long id,Model model){
        StrategyType strategyType = strategyTypeService.getStrategyTypeById(id); //根据id查询文章分类信息(回显)
        model.addAttribute("strategyType",strategyType);
        return "admin/strategyTypeUpdate";
    }

    /**
     * 文章分类修改
     *
     * @param strategyType 策略类型
     * @return {@link String}
     */
    @PostMapping("/strategyTypeUpdate")
    public String StrategyTypeUpdate(StrategyType strategyType){
        strategyTypeService.StrategyTypeUpdateById(strategyType); //文章分类修改
        return "redirect:/admin/strategyType";
    }

    /**
     * 文章类型删除
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping("/strategyType/{id}/delete")
    public String StrategyTypeDelete(@PathVariable("id")Long id,@RequestParam("pageNum")Integer pageNum){
        strategyTypeService.StrategyTypeDeleteById(id); //根据id删除文章分类
        return "redirect:/admin/strategyType/" + pageNum;
    }
}
