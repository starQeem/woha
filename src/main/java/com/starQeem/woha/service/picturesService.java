package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.Comment;
import com.starQeem.woha.pojo.Pictures;
import com.starQeem.woha.pojo.User;

import java.util.List;

/**
 * @Date: 2023/4/18 10:51
 * @author: Qeem
 */
public interface picturesService extends IService<Pictures> {
    /**
     * 新增图片
     *
     * @param pictures 图片
     * @return boolean
     */
    boolean savePictures(Pictures pictures);

    /**
     * 查询图片列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @return {@link PageInfo}<{@link Pictures}>
     */
    PageInfo<Pictures> queryPictures(Integer pageNum, int pageSize);

    /**
     * 修改图片
     *
     * @param pictures 图片
     * @return boolean
     */
    boolean updatePictures(Pictures pictures);

    /**
     * 查询我的图片详情
     *
     * @param id 图片id
     * @return {@link Pictures}
     */
    Pictures queryPicturesDetail(Long id);

    /**
     * 查询最近更新的三条我的图片记录
     *
     * @param userId 用户id
     * @return {@link List}<{@link Pictures}>
     */
    List<Pictures> getMyPicturesIndexByUpdateTime(Long userId);

    /**
     * 查询图片列表
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @param title    标题
     * @return {@link PageInfo}<{@link Pictures}>
     */
    PageInfo<Pictures> getPicturesListPageInfo(Integer pageNum, int pageSize, String title);

    /**
     * 查询图片详情
     *
     * @param id 图片id
     * @return {@link Pictures}
     */
    Pictures getPicturesDetailById(Long id);

    /**
     * 查询用户发布的图片列表
     *
     * @param id 用户id
     * @return {@link List}<{@link Pictures}>
     */
    List<Pictures> getUserPicturesWithUpdateTime(Long id);

    /**
     * 查询评论区
     *
     * @param id 图片id
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> getComments(Long id);

    /**
     * 图片点赞
     *
     * @param picturesId 图片id
     * @param userId     用户id
     * @return boolean
     */
    boolean liked(Long picturesId, Long userId);

    /**
     * 查询是否点赞
     *
     * @param picturesId 图片id
     * @param userId     用户id
     * @return boolean
     */
    boolean getStatus(Long picturesId, Long userId);

    /**
     * 查询五条图片记录
     *
     * @return {@link List}<{@link Pictures}>
     */
    List<Pictures> getPicturesListFiveBylike();

    /**
     * 删除图片
     *
     * @param id 图片id
     * @return boolean
     */
    boolean removePicturesById(Long id);

    /**
     * 统计点赞数
     *
     * @param id 图片id
     * @return {@link Integer}
     */
    Integer getLikedCount(Long id);

    /**
     * 获取点赞的前三名用户
     *
     * @param id 图片id
     * @return {@link List}<{@link User}>
     */
    List<User> getLikedUserThree(Long id);
}
