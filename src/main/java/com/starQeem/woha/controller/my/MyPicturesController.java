package com.starQeem.woha.controller.my;

import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.picturesMapper;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.picturesService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;

import static com.starQeem.woha.util.constant.PAGE_NUM;
import static com.starQeem.woha.util.constant.PICTURES_PAGE_SIZE;

/**
 * @Date: 2023/4/18 10:10
 * @author: Qeem
 * 我的图片
 */
@Controller
@RequestMapping("my/pictures")
public class MyPicturesController {
    @Resource
    private picturesService picturesService;
    @Resource
    private picturesMapper picturesMapper;
    /*
    * 查询美图
    * */
    @GetMapping(value = {"/mypictures","/mypictures/{pageNum}"})
    public String mypictures(@PathVariable(value = "pageNum",required = false)Integer pageNum, Model model){
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        PageInfo<pictures> pageInfo = picturesService.queryPictures(pageNum, PICTURES_PAGE_SIZE);
        model.addAttribute("page",pageInfo);
        return "my/pictures";
    }
    /*
    * 跳转到新增图片页面
    * */
    @GetMapping("/mypicturesInput")
    public String getmypicturesInput(){
        return "my/picturesInput";
    }
    /*
    * 新增图片
    * */
    @PostMapping("/mypicturesInput")
    public String mypicturesInput(pictures pictures, RedirectAttributes attributes){
        picturesService.savePictures(pictures);
        return "redirect:/my/pictures/mypictures";
    }
    /*
    * 跳转到图片修改页面
    * */
    @GetMapping("/mypicturesUpdate/{id}")
    public String getmypicturesUpdate(@PathVariable("id")Long id,Model model){
        pictures pictures = picturesMapper.selectById(id);
        model.addAttribute("pictures",pictures);
        return "my/picturesUpdate";
    }
    /*
    * 图片编辑
    * */
    @PostMapping("/mypicturesUpdate")
    public String mypicturesUpdate(pictures pictures){
        picturesService.updatePictures(pictures);
        return "redirect:/my/pictures/mypictures";
    }
    /*
    * 图片删除
    * */
    @RequestMapping("/mypicturesDelete/{id}")
    public String mypicturesDelete(@PathVariable("id")Long id){
        picturesService.removePicturesById(id);
        return "redirect:/my/pictures/mypictures";
    }
    /*
    * 图片详情
    * */
    @GetMapping("/mypicturesdetail/{id}")
    public String mypicturesdetail(@PathVariable("id")Long id, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Integer liked = picturesService.getLikedCount(id);
        pictures pictures = picturesService.queryPicturesDetail(id);
        List<comment> commentList = picturesService.getComments(id);//查询评论列表
        boolean status = picturesService.getStatus(id, Long.valueOf(user.getId()));//查询是否点赞
        List<user> likedUserThree = picturesService.getLikedUserThree(id);
        model.addAttribute("pictures",pictures);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/picturesdetail";
    }
}
