package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.comment;

import java.util.List;

/**
 * @Date: 2023/4/29 17:47
 * @author: Qeem
 */
public interface commentService extends IService<comment> {
    boolean Comment(comment comment);
    boolean removeComment(Long id);
    void liked(Long commentId,Long userId);
    PageInfo<comment> info(Integer pageNum, int pageSize);
    PageInfo<comment> comment(Integer pageNum, int pageSize);
}
