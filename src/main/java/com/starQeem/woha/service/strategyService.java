package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.pojo.Strategy;
import com.starQeem.woha.pojo.User;

import java.util.List;

/**
 * @Date: 2023/4/22 14:34
 * @author: Qeem
 */
public interface strategyService extends IService<Strategy> {
    /**
     * 新增文章
     *
     * @param strategy 文章
     * @return boolean
     */
    boolean saveStrategy(Strategy strategy);

    /**
     * 查询我发布的文章
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param id       用户id
     * @return {@link PageInfo}<{@link Strategy}>
     */
    PageInfo<Strategy> getUserWithStrategyWithStrategyType(Integer pageNum, int pageSize, Long id);

    /**
     * 文章回显
     *
     * @param id 文章id
     * @return {@link Strategy}
     */
    Strategy getUserWithStrategyWithStrategyTypeById(Long id);

    /**
     * 更新文章
     *
     * @param strategy 文章
     * @return boolean
     */
    boolean updateStrategy(Strategy strategy);

    /**
     * 通过id获取文章详情
     *
     * @param id 文章id
     * @return {@link Strategy}
     */
    Strategy getStrategyDetailById(Long id);

    /**
     * 查询文章分类列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param id       文章分类id
     * @param title    标题
     * @return {@link PageInfo}<{@link Strategy}>
     */
    PageInfo<Strategy> pageStrategyWithStrategyTypeById(Integer pageNum, int pageSize, Long id, String title);

    /**
     * 登录后查询文章详情
     *
     * @param id     文章id
     * @param userId 用户id
     * @return {@link Strategy}
     */
    Strategy queryStrategyDetailById(Long id, Long userId);

    /**
     * 根据文章id查询评论列表
     *
     * @param id 文章id
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> getComments(Long id);

    /**
     * 点赞
     *
     * @param strategyId 文章id
     * @param userId     用户id
     * @return boolean
     */
    boolean liked(Long strategyId, Long userId);

    /**
     * 查询是否点赞
     *
     * @param strategyId 文章id
     * @return boolean
     */
    boolean getStatus(Long strategyId);

    /**
     * 查询五条文章记录
     *
     * @return {@link List}<{@link Strategy}>
     */
    List<Strategy> getStrategyListFive();

    /**
     * 通过id删除文章
     *
     * @param id 文章id
     * @return boolean
     */
    boolean removeStrategyById(Long id);

    /**
     * 查询文章点赞数
     *
     * @param id 文章id
     * @return {@link Integer}
     */
    Integer getLikedCount(Long id);

    /**
     * 获取点赞的前三名用户
     *
     * @param id 文章id
     * @return {@link List}<{@link User}>
     */
    List<User> getLikedUserThree(Long id);
}
