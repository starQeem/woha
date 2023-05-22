package com.starQeem.woha.controller;

import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.story;
import com.starQeem.woha.pojo.strategy;
import com.starQeem.woha.service.picturesService;
import com.starQeem.woha.service.storyService;
import com.starQeem.woha.service.strategyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.List;
/**
 * @Date: 2023/4/17 0:08
 * @author: Qeem
 * 首页
 */
@Controller
public class indexController {
    @Resource
    private storyService storyService;
    @Resource
    private strategyService strategyService;
    @Resource
    private picturesService picturesService;
    /*
    * 跳转到首页
    * */
    @GetMapping
    public String index(Model model){
        List<story> storyList = storyService.getStoryListFive();
        List<strategy> strategyList = strategyService.getStrategyListFive();
        List<pictures> picturesList = picturesService.getPicturesListFiveBylike();
        model.addAttribute("storyList",storyList);
        model.addAttribute("strategyList",strategyList);
        model.addAttribute("picturesList",picturesList);
        return "index";
    }
    /*
    * 跳转到关于喔哈星页面
    * */
    @GetMapping("/more")
    public String more(){
        return "more";
    }
}
