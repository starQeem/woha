package com.starQeem.woha.controller.my;

import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.pojo.userTask;
import com.starQeem.woha.service.userTaskService;
import com.starQeem.woha.service.userService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
        user queryUser = userService.queryMyMessage();
        userTask grade = userTaskService.getGradeByUserId();
        model.addAttribute("user",queryUser);
        model.addAttribute("grade",grade);
        return "my/my";
    }
    /*
     * 跳转到我的信息编辑页面
     * */
    @GetMapping("/mymessageUpdate")
    public String getmessageUpdate(Model model){
        user users = userService.getMessage();
        model.addAttribute("user",users);
        return "my/messageUpdate";
    }
    /*
     * 我的信息编辑
     * */
    @PostMapping("/mymessageUpdate")
    public String messageUpdate(user user){
        userService.updateById(user);
        return "redirect:/my";
    }
    /*
    * 跳转到修改头像页面
    * */
    @GetMapping("/avatar")
    public String getAvatar(Model model){
        user getUser = userService.getAvatarAddress();
        model.addAttribute("user",getUser);
        return "my/avatarUpdate";
    }
    /*
    * 修改我的头像
    * */
    @PostMapping("/avatar")
    public String avatar(user user){
        userService.updateById(user);
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
    public String password(String username, String password, String newPassword,RedirectAttributes attributes){
        boolean success = userService.updatePassword(username, password, newPassword);
        if (success){
            return "redirect:/logout";
        }else {
            attributes.addFlashAttribute("message","修改失败,用户名或密码错误!");
            return "redirect:/my/password";
        }
    }
}
