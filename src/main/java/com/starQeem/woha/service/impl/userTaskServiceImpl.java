package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.userTaskMapper;
import com.starQeem.woha.pojo.userTask;
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
public class userTaskServiceImpl extends ServiceImpl<userTaskMapper, userTask> implements userTaskService {
    @Resource
    private userTaskMapper userTaskMapper;
    /*
    * 查询我的任务
    * */
    @Override
    public userTask getMyTaskByUserId() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",Long.valueOf(user.getId()));
        return userTaskMapper.selectOne(queryWrapper);
    }
    /*
    * 查询我的等级
    * */
    @Override
    public userTask getGradeByUserId() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("grade").eq("user_id",user.getId());
        return userTaskMapper.selectOne(queryWrapper);
    }
}
