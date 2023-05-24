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
    /**
     * 新增攻略
     *
     * @param strategy 攻略
     * @return boolean
     */
    boolean saveStrategy(strategy strategy);

    /**
     * 查询我发布的攻略
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param id       用户id
     * @return {@link PageInfo}<{@link strategy}>
     */
    PageInfo<strategy> getUserWithStrategyWithStrategyType(Integer pageNum, int pageSize,Long id);

    /**
     * 攻略回显
     *
     * @param id 攻略id
     * @return {@link strategy}
     */
    strategy getUserWithStrategyWithStrategyTypeById(Long id);

    /**
     * 更新攻略
     *
     * @param strategy 攻略
     * @return boolean
     */
    boolean updateStrategy(strategy strategy);

    /**
     * 通过id获取攻略详情
     *
     * @param id 攻略id
     * @return {@link strategy}
     */
    strategy getStrategyDetailById(Long id);

    /**
     * 查询攻略分类列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param id       攻略分类id
     * @param title    标题
     * @return {@link PageInfo}<{@link strategy}>
     */
    PageInfo<strategy> pageStrategyWithStrategyTypeById(Integer pageNum, int pageSize, Long id, String title);

    /**
     * 登录后查询攻略详情
     *
     * @param id     攻略id
     * @param userId 用户id
     * @return {@link strategy}
     */
    strategy queryStrategyDetailById(Long id, Long userId);

    /**
     * 根据攻略id查询评论列表
     *
     * @param id 攻略id
     * @return {@link List}<{@link comment}>
     */
    List<comment> getComments(Long id);

    /**
     * 点赞
     *
     * @param strategyId 攻略id
     * @param userId     用户id
     * @return boolean
     */
    boolean liked(Long strategyId, Long userId);

    /**
     * 查询是否点赞
     *
     * @param strategyId 攻略id
     * @return boolean
     */
    boolean getStatus(Long strategyId);

    /**
     * 查询五条攻略记录
     *
     * @return {@link List}<{@link strategy}>
     */
    List<strategy> getStrategyListFive();

    /**
     * 通过id删除攻略
     *
     * @param id 攻略id
     * @return boolean
     */
    boolean removeStrategyById(Long id);

    /**
     * 查询攻略点赞数
     *
     * @param id 攻略id
     * @return {@link Integer}
     */
    Integer getLikedCount(Long id);

    /**
     * 获取点赞的前三名用户
     *
     * @param id 攻略id
     * @return {@link List}<{@link user}>
     */
    List<user> getLikedUserThree(Long id);
}
