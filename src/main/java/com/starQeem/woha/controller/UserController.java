package com.starQeem.woha.controller;

import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.followService;
import com.starQeem.woha.service.picturesService;
import com.starQeem.woha.service.userService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.List;

import static com.starQeem.woha.util.constant.USER_FREQUENT;
import static com.starQeem.woha.util.constant.USER_FREQUENT_LIMIT;

/**
 * @Date: 2023/4/17 0:04
 * @author: Qeem
 * 用户登录
 */
@Controller
public class UserController {
    @Resource
    private userService userService;
    @Resource
    private picturesService picturesService;
    @Resource
    private followService followService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /*
     * 跳转到账号密码登录页面
     * */
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    /*
    * 跳转到验证码登录页面
    * */
    @GetMapping("/login2")
    public String login2(){
        return "login2";
    }
    /*
     * 登录
     * */
    @PostMapping("/login")
    public String loginInput(String username, String password,String code,Model model) {
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        String frequent = stringRedisTemplate.opsForValue().get(USER_FREQUENT + username);
        if (frequent != null && Integer.parseInt(frequent) >= USER_FREQUENT_LIMIT){  //判断密码或验证码是否输入错误达到5次
            model.addAttribute("message","操作过于频繁,请稍后再试");
            return "login";
        }else {
            if (password != null){ //用户名密码登录
                //封装用户的登录数据
                String md5DigestAsHex = DigestUtils.md5DigestAsHex(password.getBytes());
                UsernamePasswordToken token = new UsernamePasswordToken(username, md5DigestAsHex);
                try{
                    subject.login(token);  //执行登录的方法,如果没有异常说明ok了
                    userService.task();
                    return "redirect:/my";
                }catch (UnknownAccountException e){ //用户名不存在
                    model.addAttribute("message","用户名不存在!");
                    return "login";
                }catch (IncorrectCredentialsException e){ //密码错误
                    userService.frequent(username);
                    model.addAttribute("message","密码错误!");
                    return "login";
                }
            }else {  //邮箱验证码登录
                UsernamePasswordToken token = new UsernamePasswordToken(username, code);
                try{
                    subject.login(token);  //执行登录的方法,如果没有异常说明ok了
                    userService.task();
                    return "redirect:/my";
                }catch (UnknownAccountException e){ //用户名不存在
                    model.addAttribute("message","用户名不存在!");
                    return "login";
                }catch (IncorrectCredentialsException e){ //验证码错误
                    userService.frequent(username);
                    model.addAttribute("message","验证码错误!");
                    return "login";
                }
            }
        }
    }
    /*
     * 注销
     * */
    @GetMapping("/logout")
    public String logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/login";
    }
    /*
     * 跳转到注册页面
     * */
    @GetMapping("/register")
    public String register(){
        return "register";
    }
    /*
     * 注册
     * */
    @PostMapping("/register")
    public String registerInput(RedirectAttributes attributes,String email, String password,String code){
        boolean success = userService.saveRegister(email, password, code);
        if (success){
            return "redirect:/login";
        }else {
            attributes.addFlashAttribute("message","注册失败,验证码错误或用户已被注册!");
            return "redirect:/register";
        }
    }
    /*
    * 发送验证码
    * */
    @PostMapping("/code")
    public String sendCode(@RequestParam(value = "email",required = false)String email,
                           @RequestParam(value = "username",required = false)String username) throws MessagingException {
        if (email != null){
            userService.sendCode(email);
            return "redirect:/register";
        }else {
            userService.sendCode(username);
            return "redirect:login2";
        }
    }
    /*
    * 查看用户信息
    * */
    @GetMapping("/user/{id}")
    public String users(@PathVariable("id")Long id, Model model){
        Subject subject = SecurityUtils.getSubject();
        userDto Iuser = (userDto) subject.getPrincipal();
        if (Iuser != null){
            if (id == Long.valueOf(Iuser.getId())){   //判断是否为查询自己的信息
                model.addAttribute("ifnone",false);//是，隐藏关注和未关注按钮
            }else {
                //不是，查询是否关注
                boolean follow = followService.followSuccess(id, Long.valueOf(Iuser.getId()));
                model.addAttribute("follow",follow);
                model.addAttribute("ifnone",true);  //显示关注或者未关注按钮
            }
        }else {
            model.addAttribute("follow",false);  //未登录时显示未关注时的状态
            model.addAttribute("ifnone",true);  //显示关注或者未关注按钮
        }
        user user = userService.getUserWithGrade(id);  //查询用户等级
        List<pictures> pictures = picturesService.getUserPicturesWithUpdateTime(id);  //查询用户发布过的所有图片
        model.addAttribute("user",user);
        model.addAttribute("pictures",pictures);
        return "user";
    }
}