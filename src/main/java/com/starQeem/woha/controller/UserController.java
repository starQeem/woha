package com.starQeem.woha.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.followService;
import com.starQeem.woha.service.picturesService;
import com.starQeem.woha.service.userService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.starQeem.woha.util.constant.*;

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
    private LineCaptcha lineCaptcha;
    private String CODE = null;
    private String USERNAME = null;
    private String PASSWORD = null;
    private String MESSAGE = null;
    /*
     * 跳转到账号密码登录页面
     * */
    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("username",USERNAME);
        model.addAttribute("password",PASSWORD);
        model.addAttribute("code",CODE);
        model.addAttribute("message",MESSAGE);
        CODE = null;
        USERNAME = null;
        PASSWORD = null;
        MESSAGE = null;
        return "login";
    }
    /*
    * 跳转到验证码登录页面
    * */
    @GetMapping("/login2")
    public String login2(Model model){
        model.addAttribute("username",USERNAME);
        model.addAttribute("code",CODE);
        model.addAttribute("message",MESSAGE);
        CODE = null;
        PASSWORD = null;
        MESSAGE = null;
        return "login2";
    }
    /*
     * 登录
     * */
    @PostMapping("/login")
    public String loginInput(String username, String password,String code) {
        //查询账号状态
        boolean UserStatus = userService.getUserStatus(username);
        if (!UserStatus){
            MESSAGE = "账号已被禁用,请联系管理员!";
            return "redirect:/login";
        }
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
            if (password != null){ //用户名密码登录
                if (lineCaptcha.getCode().equals(code)){
                    //封装用户的登录数据
                    String md5DigestAsHex = DigestUtils.md5DigestAsHex(password.getBytes());
                    UsernamePasswordToken token = new UsernamePasswordToken(username, md5DigestAsHex);
                    try{
                        subject.login(token);  //执行登录的方法,如果没有异常说明ok了
                        userService.task();
                        return "redirect:/my";
                    }catch (UnknownAccountException e){ //用户名不存在
                        USERNAME = username;
                        PASSWORD = password;
                        MESSAGE = "用户名不存在!";
                        return "redirect:/login";
                    }catch (IncorrectCredentialsException e){ //密码错误
                        USERNAME = username;
                        PASSWORD = password;
                        MESSAGE = "密码错误!";
                        return "redirect:/login";
                }
                }else {
                    USERNAME = username;
                    PASSWORD = password;
                    CODE  = code;
                    MESSAGE = "验证码错误!";
                    return "redirect:/login";
                }
            }else {  //邮箱验证码登录
                UsernamePasswordToken token = new UsernamePasswordToken(username, code);
                try{
                    subject.login(token);  //执行登录的方法,如果没有异常说明ok了
                    userService.task();
                    return "redirect:/my";
                }catch (UnknownAccountException e){ //用户名不存在
                    USERNAME = username;
                    CODE  = code;
                    MESSAGE = "用户名不存在!";
                    return "redirect:/login2";
                }catch (IncorrectCredentialsException e){ //验证码错误
                    USERNAME = username;
                    CODE  = code;
                    MESSAGE = "验证码错误!";
                    return "redirect:/login2";
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
    public String register(Model model){
        model.addAttribute("email",USERNAME);
        model.addAttribute("password",PASSWORD);
        model.addAttribute("code",CODE);
        model.addAttribute("message",MESSAGE);
        CODE = null;
        USERNAME = null;
        PASSWORD = null;
        MESSAGE = null;
        return "register";
    }
    /*
     * 注册
     * */
    @PostMapping("/register")
    public String registerInput(String email, String password,String code){
        String getCode = stringRedisTemplate.opsForValue().get(USER_CODE + email);
        if (!code.equals(getCode)){
            USERNAME = email;
            PASSWORD = password;
            CODE = code;
            MESSAGE = "验证码错误!";
            return "redirect:/register";
        }
        boolean success = userService.saveRegister(email, password);
        if (success){
            return "redirect:/login";
        }else {
            USERNAME = email;
            PASSWORD = password;
            CODE = code;
            MESSAGE = "注册失败,用户已被注册!";
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
     * 图片验证码
     */
    @GetMapping("/picturesCode")
    public void getCode(HttpServletResponse response) {
        // 随机生成 4 位验证码
        RandomGenerator randomGenerator = new RandomGenerator(PICTURES_CODE_RANDOM, PICTURES_CODE_LENGTH);
        // 定义图片的显示大小
        lineCaptcha = CaptchaUtil.createLineCaptcha(110, 36);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        try {
            // 调用父类的 setGenerator() 方法，设置验证码的类型
            lineCaptcha.setGenerator(randomGenerator);
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
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
            if (Objects.equals(id, Long.valueOf(Iuser.getId()))){   //判断是否为查询自己的信息
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
