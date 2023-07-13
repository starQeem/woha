package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.pojo.Story;
import com.starQeem.woha.pojo.User;

import java.util.List;

/**
 * @Date: 2023/4/17 15:42
 * @author: Qeem
 */
public interface storyService extends IService<Story>{
    /**
     * 新增问答
     *
     * @param story 问答
     * @return boolean
     */
    boolean saveStory(Story story);

    /**
     * 查询我的问答问答列表
     *
     * @param pageNum   页码
     * @param PAGE_SIZE 数据条数
     * @param id        用户id
     * @return {@link PageInfo}<{@link Story}>
     */
    PageInfo<Story> queryMyStory(Integer pageNum, int PAGE_SIZE, Long id);

    /**
     * 登录后查询问答问答详情
     *
     * @param id id
     * @return {@link Story}
     */
    Story queryStoryDetail(Long id);

    /**
     * 更新问答问答
     *
     * @param story 问答
     * @return boolean
     */
    boolean updateStory(Story story);


    /**
     * 查询问答问答列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param title    问答标题
     * @return {@link PageInfo}<{@link Story}>
     */
    PageInfo<Story> getStoryListPageInfo(Integer pageNum, int pageSize, String title);

    /**
     * 查询问答问答详情
     *
     * @param id 问答id
     * @return {@link Story}
     */
    Story getStoryById(Long id);

    /**
     * 查询评论列表
     *
     * @param id 问答id
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> getComments(Long id);

    /**
     * 点赞
     *
     * @param storyId 问答id
     * @param userId  用户id
     * @return boolean
     */
    boolean liked(Long storyId, Long userId);

    /**
     * 查询是否点赞
     *
     * @param storyId 问答id
     * @return boolean
     */
    boolean getStatus(Long storyId);

    /**
     * 得到五条问答信息
     *
     * @return {@link List}<{@link Story}>
     */
    List<Story> getStoryListFive();

    /**
     * 通过id删除问答
     *
     * @param id id
     * @return boolean
     */
    boolean removeStoryById(Long id);

    /**
     * 查询问答文章点赞数
     *
     * @param id 问答id
     * @return {@link Integer}
     */
    Integer getLikedCount(Long id);

    /**
     * 获取点赞的前三名用户
     *
     * @param id 问答id
     * @return {@link List}<{@link User}>
     */
    List<User> getLikedUserThree(Long id);
}
