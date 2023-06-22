package com.starQeem.woha.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.config.email;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.userMapper;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.pojo.userTask;
import com.starQeem.woha.service.userTaskService;
import com.starQeem.woha.service.userService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/17 0:03
 * @author: Qeem
 */
@Service
public class userServiceImpl extends ServiceImpl<userMapper, user> implements userService {
    @Resource
    private userMapper userMapper;
    @Resource
    private userService userService;
    @Resource
    private userTaskService userTaskService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private email emailComponent;

    /*
     * 注册
     * */
    @Override
    public boolean saveRegister(String email, String password) {
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "email", "password", "username").eq("username", email);
        user user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            return false;
        } else {
            user newUser = new user();
            String md5DigestAsHex = DigestUtils.md5DigestAsHex(password.getBytes());
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setPassword(md5DigestAsHex);
            newUser.setStatus(STATUS_ONE);
            return userService.save(newUser);
        }
    }

    /*
     * 发送验证码
     * */
    @Override
    public void sendCode(String email) throws MessagingException {
        //生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        System.out.println(code);
        emailComponent.sendVerificationCode(EMAIL_FORM, email, code);  //发送验证码
        //将验证码存入redis中
        stringRedisTemplate.opsForValue().set(USER_CODE + email, code, TIME_CODE, TimeUnit.SECONDS);
    }

    /*
     * 获取用户信息
     * */
    @Override
    public user queryMyMessage() {  //用户注册后第一次登录时设置默认信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("id", "email", "sex", "signature", "hobby", "nick_name", "avatar")
                .eq("id", Long.valueOf(user.getId()));
        user queryUser = userMapper.selectOne(queryWrapper);
        if (queryUser.getEmail() == null || queryUser.getEmail().equals("")) {
            queryUser.setEmail(USER_MESSAGE);
        }
        if (queryUser.getSex() == null || queryUser.getSex().equals("")) {
            queryUser.setSex(USER_MESSAGE);
        }
        if (queryUser.getSignature() == null || queryUser.getSignature().equals("")) {
            queryUser.setSignature(USER_SIGNATURE);
        }
        if (queryUser.getHobby() == null || queryUser.getHobby().equals("")) {
            queryUser.setHobby(USER_MESSAGE);
        }
        if (queryUser.getNickName() == null || queryUser.getNickName().equals("")) {
            queryUser.setNickName(USER_NICK + RandomUtil.randomString(10));
        }
        if (queryUser.getAvatar() == null || queryUser.getAvatar().equals("")) {
            queryUser.setAvatar(USER_AVATAR);
        }
        userService.updateById(queryUser);
        return queryUser;
    }

    /*
     * 我的信息编辑回显
     * */
    @Override
    public user getMessage() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("id", "nick_name", "sex", "email", "signature", "hobby")
                .eq("id", Long.valueOf(user.getId()));
        user userMessage = userMapper.selectOne(queryWrapper);
        return userMessage;
    }

    /*
     * 任务
     * */
    @Override
    public void task() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", Long.valueOf(user.getId()));
        userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
        if (userTask == null) {  //用户注册后第一次访问任务页面设置默认值
            userTask task = new userTask();
            task.setUserId(Long.valueOf(user.getId()));
            task.setGrade(GRADE);  //默认等级
            task.setDailytaskStrategy(STATUS_ZERO);
            task.setDailytaskStory(STATUS_ZERO);
            task.setDailytaskLogin(STATUS_ZERO);
            task.setWeeklytaskPictures(STATUS_ZERO);
            task.setExperience(STATUS_ZERO);
            boolean success = userTaskService.save(task);
            if (success) {
                userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            }
        }
        if (userTask.getDailytaskLogin() == STATUS_ZERO) {  //判断今日是否未登录过
            Integer experience = userTask.getExperience();
            userTask.setExperience(experience + TASK_DAY_EXPERIENCE); //经验+
            userTask.setDailytaskLogin(STATUS_ONE);  //设置状态为已完成
        }
        userTaskService.updateById(userTask);
    }

    /*
     * 查询用户信息
     * */
    @Override
    public user getUserWithGrade(Long id) {
        user user = userMapper.getUserWithGrade(id);
        return user;
    }

    /*
     * 获取用户头像
     * */
    @Override
    public user getAvatarAddress() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "avatar").eq("id", Long.valueOf(user.getId()));
        return userMapper.selectOne(queryWrapper);
    }

    /*
     * 修改我的密码
     * */
    @Override
    public boolean updatePassword(String username, String password, String newPassword) {
        Subject subject = SecurityUtils.getSubject();
        userDto userDto = (userDto) subject.getPrincipal();
        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "username", "password")
                .eq("username", username)
                .eq("password", md5Password)
                .eq("id", Long.valueOf(userDto.getId()));
        user user = userMapper.selectOne(queryWrapper);
        if (user != null) {  //有值,说明用户名密码正确
            user.setPassword(md5NewPassword);
            int i = userMapper.updateById(user);
            if (i > 0) {
                return true;
            } else {
                return false;
            }
        } else {  //没有值,用户名错误
            return false;
        }
    }

    /**
     * 更新用户状态
     *
     * @param id id
     */
    @Override
    public void updateStatus(Long id) {
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","status").eq("id",id);
        user user = userMapper.selectOne(queryWrapper);
        if (user.getStatus() == STATUS_ONE){
            user.setStatus(STATUS_ZERO);
        }else {
            user.setStatus(STATUS_ONE);
        }
        userMapper.updateById(user);
    }

    /**
     * 得到用户状态
     *
     * @param username 用户名
     * @return boolean
     */
    @Override
    public boolean getUserStatus(String username) {
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        user user = userMapper.selectOne(queryWrapper);
        if (user.getStatus() == STATUS_ONE){
            return true;
        }
        return false;
    }

}
