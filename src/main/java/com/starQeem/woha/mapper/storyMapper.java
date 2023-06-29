package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.story;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/17 15:41
 * @author: Qeem
 */
@Mapper
public interface storyMapper extends BaseMapper<story> {
    List<story> getUserWithStory(Long userId);
    story getUserWithStoryById(Long id,Long userId);
    List<story> getStory(String title);
    story getStoryById(Long id);
}
