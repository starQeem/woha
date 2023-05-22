package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.strategy;
import com.starQeem.woha.pojo.user;

import java.util.List;

/**
 * @Date: 2023/4/22 14:34
 * @author: Qeem
 */
public interface strategyService extends IService<strategy> {
    boolean saveStrategy(strategy strategy);
    PageInfo<strategy> getUserWithStrategyWithStrategyType(Integer pageNum, int pageSize,Long id);
    strategy getUserWithStrategyWithStrategyTypeById(Long id);
    boolean updateStrategy(strategy strategy);
    strategy getStrategyDetailById(Long id);
    PageInfo<strategy> pageStrategyWithStrategyTypeById(Integer pageNum, int pageSize, Long id, String title);
    strategy queryStrategyDetailById(Long id, Long userId);
    List<comment> getComments(Long id);
    boolean liked(Long strategyId, Long userId);
    boolean getStatus(Long strategyId);
    List<strategy> getStrategyListFive();
    boolean removeStrategyById(Long id);
    Integer getLikedCount(Long id);
    List<user> getLikedUserThree(Long id);
}
