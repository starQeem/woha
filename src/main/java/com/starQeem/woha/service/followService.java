package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.follow;
import com.starQeem.woha.pojo.user;

import java.util.List;

/**
 * @Date: 2023/4/28 17:16
 * @author: Qeem
 */
public interface followService extends IService<follow> {
    void savefollow(Long userId,Long followId);
    boolean followSuccess(Long id,Long IuserId);
    List<user> getfollowList();
    List<user> getFansList();
}
