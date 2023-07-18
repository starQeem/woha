package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.followMapper;
import com.starQeem.woha.mapper.userMapper;
import com.starQeem.woha.pojo.Follow;
import com.starQeem.woha.pojo.User;
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
public class followServiceImpl extends ServiceImpl<followMapper, Follow> implements followService {
    @Resource
    private userMapper userMapper;
    /*
    * 关注和取关
    * */
    @Override
    public void saveFollow(Long userId,Long followId) {
        Follow getFollow = getBaseMapper().selectOne(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId,userId)
                .eq(Follow::getFollowUserId,followId));
        Follow follow = new Follow();
        if (getFollow != null){
            removeById(getFollow.getId());   //已关注,删除关注记录
        }else {
            follow.setUserId(userId);
            follow.setFollowUserId(followId);
            save(follow);  //未关注,新增关注记录
        }
    }
    /*
    * 查询是否关注该用户
    * */
    @Override
    public boolean followSuccess(Long id,Long IuserId) {
        Integer count = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId,IuserId)
                .eq(Follow::getFollowUserId,id));
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
    public List<User> getFollowList() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.getFollowUser(Long.valueOf(user.getId()));
    }
    /*
    * 查询关注我的列表
    * */
    @Override
    public List<User> getFansList() {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        return userMapper.getFansUser(Long.valueOf(user.getId()));
    }
}
