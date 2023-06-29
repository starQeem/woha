package com.starQeem.woha.controller.my;

import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.followService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Date: 2023/4/28 23:01
 * @author: Qeem
 * 我的关注和粉丝
 */
@Controller
@RequestMapping("my")
public class MyFollowController {
    @Resource
    private followService followService;
    /*
    * 我的关注
    * */
    @GetMapping("/follow")
    public String follow(Model model){
        List<user> userList = followService.getFollowList(); //查询我关注的用户列表
        model.addAttribute("userList",userList);
        return "my/follow";
    }
    /*
    * 我的粉丝
    * */
    @GetMapping("/fans")
    public String fans(Model model){
        List<user> userList = followService.getFansList();  //查询关注我的用户列表
        model.addAttribute("userList",userList);
        return "my/fans";
    }
}
