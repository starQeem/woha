package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.Pictures;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/18 10:50
 * @author: Qeem
 */
@Mapper
public interface picturesMapper extends BaseMapper<Pictures> {
    List<Pictures> getUserWithPictures(Long userId);
    Pictures queryPicturesWithUserById(Long id, Long userId);
    List<Pictures> getMyPicturesIndexByUpdateTime(Long userId);
    List<Pictures> getPicturesListPageInfo(String title);
    Pictures getPicturesDetailById(Long id);
}
