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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    * 查询壁纸
    * */
    @GetMapping(value = {"/mypictures","/mypictures/{pageNum}"})
    public String myPictures(@PathVariable(value = "pageNum",required = false)Integer pageNum, Model model){
        PageInfo<pictures> pageInfo = picturesService.queryPictures(pageNum, PICTURES_PAGE_SIZE); //查询图片列表
        model.addAttribute("page",pageInfo);
        return "my/pictures";
    }
    /*
    * 跳转到新增图片页面
    * */
    @GetMapping("/mypicturesInput")
    public String getMyPicturesInput(){
        return "my/picturesInput";
    }
    /*
    * 新增图片
    * */
    @PostMapping("/mypicturesInput")
    public String myPicturesInput(pictures pictures){
        picturesService.savePictures(pictures); //新增壁纸图片
        return "redirect:/my/pictures/mypictures";
    }
    /*
    * 跳转到图片修改页面
    * */
    @GetMapping("/mypicturesUpdate/{id}")
    public String getMyPicturesUpdate(@PathVariable("id")Long id,Model model){
        pictures pictures = picturesMapper.selectById(id);  //根据id查询图片信息
        model.addAttribute("pictures",pictures);
        return "my/picturesUpdate";
    }
    /*
    * 图片编辑
    * */
    @PostMapping("/mypicturesUpdate")
    public String myPicturesUpdate(pictures pictures){
        picturesService.updatePictures(pictures);  //修改图片信息
        return "redirect:/my/pictures/mypictures";
    }
    /*
    * 图片删除
    * */
    @RequestMapping("/mypicturesDelete/{id}")
    public String mypicturesDelete(@PathVariable("id")Long id, @RequestParam(value = "pageNum",required = false)Integer pageNum){
        picturesService.removePicturesById(id);  //根据id删除图片
        if (pageNum == null){ //页码为空是管理员删除
            return "redirect:/pictures";
        }
        return "redirect:/my/pictures/mypictures/" + pageNum;
    }
    /*
    * 图片详情
    * */
    @GetMapping("/mypicturesdetail/{id}")
    public String myPicturesDetail(@PathVariable("id")Long id, Model model){
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Integer liked = picturesService.getLikedCount(id); //根据图片id查询图片点赞数
        pictures pictures = picturesService.queryPicturesDetail(id);  //根据图片id查询图片信息
        List<comment> commentList = picturesService.getComments(id);//根据图片id查询评论列表
        boolean status = picturesService.getStatus(id, Long.valueOf(user.getId()));//查询是否点赞
        List<user> likedUserThree = picturesService.getLikedUserThree(id); //获取前三名点赞的用户
        model.addAttribute("pictures",pictures);
        model.addAttribute("commentsList",commentList);
        model.addAttribute("status",status);
        model.addAttribute("userId",Long.valueOf(user.getId()));
        model.addAttribute("liked",liked);
        model.addAttribute("likedUserThree",likedUserThree);
        return "my/picturesdetail";
    }
}
