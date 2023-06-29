package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.story;
import com.starQeem.woha.pojo.user;

import java.util.List;

/**
 * @Date: 2023/4/17 15:42
 * @author: Qeem
 */
public interface storyService extends IService<story>{
    /**
     * 新增故事文章
     *
     * @param story 故事
     * @return boolean
     */
    boolean saveStory(story story);

    /**
     * 查询我的故事文章列表
     *
     * @param pageNum   页码
     * @param PAGE_SIZE 数据条数
     * @param id        用户id
     * @return {@link PageInfo}<{@link story}>
     */
    PageInfo<story> queryMyStory(Integer pageNum,int PAGE_SIZE,Long id);

    /**
     * 登录后查询故事文章详情
     *
     * @param id id
     * @return {@link story}
     */
    story queryStoryDetail(Long id);

    /**
     * 更新故事文章
     *
     * @param story 故事
     * @return boolean
     */
    boolean updateStory(story story);


    /**
     * 查询故事文章列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param title    文章标题
     * @return {@link PageInfo}<{@link story}>
     */
    PageInfo<story> getStoryListPageInfo(Integer pageNum, int pageSize, String title);

    /**
     * 查询故事文章详情
     *
     * @param id 故事id
     * @return {@link story}
     */
    story getStoryById(Long id);

    /**
     * 查询评论列表
     *
     * @param id 故事id
     * @return {@link List}<{@link comment}>
     */
    List<comment> getComments(Long id);

    /**
     * 点赞
     *
     * @param storyId 故事id
     * @param userId  用户id
     * @return boolean
     */
    boolean liked(Long storyId, Long userId);

    /**
     * 查询是否点赞
     *
     * @param storyId 故事id
     * @return boolean
     */
    boolean getStatus(Long storyId);

    /**
     * 得到五条故事信息
     *
     * @return {@link List}<{@link story}>
     */
    List<story> getStoryListFive();

    /**
     * 通过id删除故事文章
     *
     * @param id id
     * @return boolean
     */
    boolean removeStoryById(Long id);

    /**
     * 查询故事文章点赞数
     *
     * @param id 故事id
     * @return {@link Integer}
     */
    Integer getLikedCount(Long id);

    /**
     * 获取点赞的前三名用户
     *
     * @param id 故事id
     * @return {@link List}<{@link user}>
     */
    List<user> getLikedUserThree(Long id);
}
