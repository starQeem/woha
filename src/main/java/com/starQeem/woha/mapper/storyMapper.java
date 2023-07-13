package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.Story;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/17 15:41
 * @author: Qeem
 */
@Mapper
public interface storyMapper extends BaseMapper<Story> {
    List<Story> getUserWithStory(Long userId);
    Story getUserWithStoryById(Long id, Long userId);
    List<Story> getStory(String title);
    Story getStoryById(Long id);
}
