package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/29 17:46
 * @author: Qeem
 */
@Mapper
public interface commentMapper extends BaseMapper<comment> {
    List<comment> commentPicturesInfo(Long userId);
    List<comment> getPicturesComments(Long picturesId);
    List<comment> getStoryComments(Long storyId);
    List<comment> getStrategyComments(Long strategyId);
    List<comment> commentStrategyInfo(Long valueOf);
    List<comment> commentStoryInfo(Long valueOf);
    List<comment> picturesComment(Long valueOf);
    List<comment> strategyComment(Long valueOf);
    List<comment> storyComment(Long valueOf);
}
