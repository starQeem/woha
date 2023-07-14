package com.starQeem.woha.controller.my;

import com.starQeem.woha.pojo.User;
import com.starQeem.woha.pojo.UserTask;
import com.starQeem.woha.service.userTaskService;
import com.starQeem.woha.service.userService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

/**
 * @Date: 2023/4/17 12:46
 * @author: Qeem
 * 我的
 */
@Controller
@RequestMapping("/my")
public class MyController {
    @Resource
    private userService userService;
    @Resource
    private userTaskService userTaskService;
    /*
     * 查询我的信息
     * */
    @GetMapping
    public String User(Model model){
        User queryUser = userService.queryMyMessage(); //查询用户信息
        UserTask grade = userTaskService.getGradeByUserId();  //查询用户等级信息
        model.addAttribute("user",queryUser);
        model.addAttribute("grade",grade);
        return "my/my";
    }
    /*
     * 跳转到我的信息编辑页面
     * */
    @GetMapping("/mymessageUpdate")
    public String getMessageUpdate(Model model){
        User user = userService.getMessage(); //查询我的信息(回显)
        model.addAttribute("user",user);
        return "my/messageUpdate";
    }
    /*
     * 我的信息编辑
     * */
    @PostMapping("/mymessageUpdate")
    public String messageUpdate(User user){
        userService.updateById(user); //修改我的信息
        return "redirect:/my";
    }
    /*
    * 跳转到修改头像页面
    * */
    @GetMapping("/avatar")
    public String getAvatar(Model model){
        User user = userService.getAvatarAddress(); //查询用户信息
        model.addAttribute("user",user);
        return "my/avatarUpdate";
    }
    /*
    * 修改我的头像
    * */
    @PostMapping("/avatar")
    public String avatar(User user){
        userService.updateById(user); //修改我的头像
        return "redirect:/my";
    }
    /*
    * 跳转到修改密码页面
    * */
    @GetMapping("/password")
    public String getPassword(){
        return "my/passwordUpdate";
    }
    /*
    * 修改我的密码
    * */
    @PostMapping("/password")
    public String password(String password, String newPassword,RedirectAttributes attributes){
        boolean success = userService.updatePassword(password, newPassword); //修改我的密码
        if (success){
            return "redirect:/logout";
        }else {
            attributes.addFlashAttribute("message","修改失败,密码错误!");
            return "redirect:/my/password";
        }
    }
}
