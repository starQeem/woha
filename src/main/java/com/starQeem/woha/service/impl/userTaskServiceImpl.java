package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.userTaskMapper;
import com.starQeem.woha.pojo.UserTask;
import com.starQeem.woha.service.userTaskService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Date: 2023/4/22 14:58
 * @author: Qeem
 */
@Service
public class userTaskServiceImpl extends ServiceImpl<userTaskMapper, UserTask> implements userTaskService {
    @Resource
    private userTaskMapper userTaskMapper;
    /*
    * 查询我的任务
    * */
    @Override
    public UserTask getMyTaskByUserId() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userTaskMapper.selectOne(Wrappers.<UserTask>lambdaQuery().eq(UserTask::getUserId,Long.valueOf(user.getId())));
    }
    /*
    * 查询我的等级
    * */
    @Override
    public UserTask getGradeByUserId() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userTaskMapper.selectOne(Wrappers.<UserTask>lambdaQuery().select(UserTask::getGrade).eq(UserTask::getUserId,user.getId()));
    }
}
