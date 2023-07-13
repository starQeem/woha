package com.starQeem.woha.controller;

import com.starQeem.woha.pojo.Pictures;
import com.starQeem.woha.pojo.Story;
import com.starQeem.woha.pojo.Strategy;
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
        List<Story> storyList = storyService.getStoryListFive(); //查询五条问答列表信息
        List<Strategy> strategyList = strategyService.getStrategyListFive(); //查询五条文章列表信息
        List<Pictures> picturesList = picturesService.getPicturesListFiveBylike();  //查询图片列表
        model.addAttribute("storyList",storyList);
        model.addAttribute("strategyList",strategyList);
        model.addAttribute("picturesList",picturesList);
        return "index";
    }
    /*
    * 跳转到联系管理员页面
    * */
    @GetMapping("/more")
    public String more(){
        return "more";
    }
}
