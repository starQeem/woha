package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/29 17:46
 * @author: Qeem
 */
@Mapper
public interface commentMapper extends BaseMapper<Comment> {
    List<Comment> commentPicturesInfo(Long userId);
    List<Comment> getPicturesComments(Long picturesId);
    List<Comment> getStoryComments(Long storyId);
    List<Comment> getStrategyComments(Long strategyId);
    List<Comment> commentStrategyInfo(Long valueOf);
    List<Comment> commentStoryInfo(Long valueOf);
    List<Comment> picturesComment(Long valueOf);
    List<Comment> strategyComment(Long valueOf);
    List<Comment> storyComment(Long valueOf);
}
