package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.followMapper;
import com.starQeem.woha.mapper.userMapper;
import com.starQeem.woha.pojo.follow;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.followService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Date: 2023/4/28 17:17
 * @author: Qeem
 */
@Service
public class followServiceImpl extends ServiceImpl<followMapper, follow> implements followService {
    @Resource
    private followService followService;
    @Resource
    private userMapper userMapper;
    /*
    * 关注和取关
    * */
    @Override
    public void saveFollow(Long userId,Long followId) {
        QueryWrapper<follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).eq("follow_user_id",followId);
        follow getFollow = followService.getBaseMapper().selectOne(queryWrapper);
        follow follow = new follow();
        if (getFollow != null){
            followService.removeById(getFollow.getId());   //已关注,删除关注记录
        }else {
            follow.setUserId(userId);
            follow.setFollowUserId(followId);
            followService.save(follow);  //未关注,新增关注记录
        }
    }
    /*
    * 查询是否关注该用户
    * */
    @Override
    public boolean followSuccess(Long id,Long IuserId) {
        QueryWrapper<follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",IuserId).eq("follow_user_id",id);
        Integer count = followService.getBaseMapper().selectCount(queryWrapper);
        if (count > 0){
            return true;
        }else {
            return false;
        }
    }
    /*
    * 查询关注列表
    * */
    @Override
    public List<user> getFollowList() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.getFollowUser(Long.valueOf(user.getId()));
    }
    /*
    * 查询关注我的列表
    * */
    @Override
    public List<user> getFansList() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.getFansUser(Long.valueOf(user.getId()));
    }
}
