package com.starQeem.woha.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.config.email;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.userMapper;
import com.starQeem.woha.pojo.User;
import com.starQeem.woha.pojo.UserTask;
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
public class userServiceImpl extends ServiceImpl<userMapper, User> implements userService {
    @Resource
    private userMapper userMapper;
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
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getEmail,User::getPassword,User::getUsername)
                .eq(User::getUsername,email));
        if (user != null) {
            return false;
        } else {
            User newUser = new User();
            String md5DigestAsHex = DigestUtils.md5DigestAsHex(password.getBytes());
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setPassword(md5DigestAsHex);
            newUser.setStatus(STATUS_ONE);
            return save(newUser);
        }
    }

    /*
     * 发送验证码
     * */
    @Override
    public void sendCode(String email) throws MessagingException {
        //生成6位随机验证码
        String code = RandomUtil.randomNumbers(CODE_SIZE);
        System.out.println(code);
        emailComponent.sendVerificationCode(EMAIL_FORM, email, code);  //发送验证码
        //将验证码存入redis中
        stringRedisTemplate.opsForValue().set(USER_CODE + email, code, TIME_CODE, TimeUnit.SECONDS);
    }

    /*
     * 获取用户信息
     * */
    @Override
    public User queryMyMessage() {  //用户注册后第一次登录时设置默认信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        User queryUser = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getId,Long.valueOf(user.getId())));
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
            queryUser.setNickName(USER_NICK + RandomUtil.randomString(DEFAULT_USER_NICK_NAME_SIZE));
        }
        if (queryUser.getAvatar() == null || queryUser.getAvatar().equals("")) {
            queryUser.setAvatar(USER_AVATAR);
        }
        updateById(queryUser);
        return queryUser;
    }

    /*
     * 我的信息编辑回显
     * */
    @Override
    public User getMessage() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getId,Long.valueOf(user.getId())));
    }

    /*
     * 任务
     * */
    @Override
    public void task() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        UserTask userTask = userTaskService.getBaseMapper().selectOne(Wrappers.<UserTask>lambdaQuery()
                .eq(UserTask::getUserId,Long.valueOf(user.getId())));
        if (userTask == null) {  //用户注册后第一次访问任务页面设置默认值
            UserTask task = new UserTask();
            task.setUserId(Long.valueOf(user.getId()));
            task.setGrade(GRADE);  //默认等级
            task.setDailytaskStrategy(STATUS_ZERO);
            task.setDailytaskStory(STATUS_ZERO);
            task.setDailytaskLogin(STATUS_ZERO);
            task.setWeeklytaskPictures(STATUS_ZERO);
            task.setExperience(STATUS_ZERO);
            boolean success = userTaskService.save(task);
            if (success) {
                userTask = userTaskService.getBaseMapper().selectOne(Wrappers.<UserTask>lambdaQuery()
                        .eq(UserTask::getUserId,Long.valueOf(user.getId())));
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
    public User getUserWithGrade(Long id) {
        return userMapper.getUserWithGrade(id);
    }

    /*
     * 获取用户头像
     * */
    @Override
    public User getAvatarAddress() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getAvatar)
                .eq(User::getId,Long.valueOf(user.getId())));
    }

    /*
     * 修改我的密码
     * */
    @Override
    public boolean updatePassword( String password, String newPassword) {
        Subject subject = SecurityUtils.getSubject();
        userDto userDto = (userDto) subject.getPrincipal();
        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getUsername,User::getPassword)
                .eq(User::getUsername,userDto.getUsername())
                .eq(User::getPassword,md5Password)
                .eq(User::getId,Long.valueOf(userDto.getId())));
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
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().select(User::getId,User::getStatus).eq(User::getId,id));
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
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,username));
        if (user.getStatus() == STATUS_ONE){
            return true;
        }
        return false;
    }

    /**
     * 获取用户列表
     *
     * @param pageNum 页面num
     * @return {@link PageInfo}<{@link User}>
     */
    @Override
    public PageInfo<User> getUserList(Integer pageNum, String nickName) {
        int pageSize = PAGE_SIZE;
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        if (nickName == null) {
            nickName = "";
        } else {
            pageSize = SEARCH_SIZE;
        }
        PageHelper.startPage(pageNum, pageSize);
        //查询数据库
        List<User> userList = userMapper.getUserList(nickName);
        //将List集合丢到分页对象里
        return new PageInfo<>(userList, pageSize);
    }

}
