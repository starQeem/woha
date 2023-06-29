package com.starQeem.woha.controller;

import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.service.followService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * @Date: 2023/4/28 17:18
 * @author: Qeem
 * 关注和取关
 */
@Controller
public class followController {
    @Resource
    private followService followService;
    @GetMapping("/follow/{id}")
    public String follow(@PathVariable("id") Long id){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        if (user != null){
            followService.saveFollow(Long.valueOf(user.getId()),id);
            return "redirect:/user/{id}";
        }else {
            return "redirect:/login";  //未登录跳转到登录页面
        }
    }
}
