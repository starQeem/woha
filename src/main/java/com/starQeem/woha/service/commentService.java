package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.Comment;

import javax.mail.MessagingException;

/**
 * @Date: 2023/4/29 17:47
 * @author: Qeem
 */
public interface commentService extends IService<Comment> {
    /**
     * 评论
     *
     * @param comment 评论
     * @return boolean
     */
    boolean Comment(Comment comment) throws MessagingException;

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return boolean
     */
    boolean removeComment(Long id);

    /**
     * 点赞
     *
     * @param commentId 评论id
     * @param userId    用户id
     */
    void liked(Long commentId,Long userId);

    /**
     * 回复我的评论
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @return {@link PageInfo}<{@link Comment}>
     */
    PageInfo<Comment> info(Integer pageNum, int pageSize);

    /**
     * 评论
     *
     * @param pageNum  页码
     * @param pageSize 数据条数
     * @return {@link PageInfo}<{@link Comment}>
     */
    PageInfo<Comment> comment(Integer pageNum, int pageSize);
}
