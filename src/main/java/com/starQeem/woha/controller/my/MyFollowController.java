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
    public String follow(Model model,String nickName){
        if (nickName == null){
           nickName = "";
        }
        List<user> userList = followService.getfollowList();
        model.addAttribute("userList",userList);
        return "my/follow";
    }
    /*
    * 我的粉丝
    * */
    @GetMapping("/fans")
    public String fans(Model model,String nickName){
        if (nickName == null){
            nickName = "";
        }
        List<user> userList = followService.getFansList();
        model.addAttribute("userList",userList);
        return "my/fans";
    }
}
