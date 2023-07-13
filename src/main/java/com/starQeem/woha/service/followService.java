package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.Follow;
import com.starQeem.woha.pojo.User;

import java.util.List;

/**
 * @Date: 2023/4/28 17:16
 * @author: Qeem
 */
public interface followService extends IService<Follow> {
    /**
     * 关注和取关
     *
     * @param userId   用户id
     * @param followId 需要关注的用户id
     */
    void saveFollow(Long userId,Long followId);

    /**
     * 是否关注用户
     *
     * @param id      用户id
     * @param IuserId 我的用户id
     * @return boolean
     */
    boolean followSuccess(Long id,Long IuserId);

    /**
     * 查询关注列表
     *
     * @return {@link List}<{@link User}>
     */
    List<User> getFollowList();

    /**
     * 查询关注我的列表
     *
     * @return {@link List}<{@link User}>
     */
    List<User> getFansList();
}
