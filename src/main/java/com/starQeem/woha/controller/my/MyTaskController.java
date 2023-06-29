package com.starQeem.woha.controller.my;

import com.starQeem.woha.pojo.userTask;
import com.starQeem.woha.service.userTaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @Date: 2023/4/24 17:41
 * @author: Qeem
 * 我的任务
 */
@Controller
@RequestMapping("/my/task")
public class MyTaskController {
    @Resource
    private userTaskService userTaskService;
    /*
    * 跳转到我的任务页面
    * */
    @GetMapping
    public String task(Model model){
        userTask userTasks = userTaskService.getMyTaskByUserId(); //查询我的任务信息
        model.addAttribute("task",userTasks);
        return "my/task";
    }
}
