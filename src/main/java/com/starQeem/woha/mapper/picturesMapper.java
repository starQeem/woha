package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.pictures;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/18 10:50
 * @author: Qeem
 */
@Mapper
public interface picturesMapper extends BaseMapper<pictures> {
    List<pictures> getUserWithPictures(Long userId);
    pictures queryPicturesWithUserById(Long id,Long userId);
    List<pictures> getMyPicturesIndexByUpdateTime(Long userId);
    List<pictures> getPicturesListPageInfo(String title);
    pictures getPicturesDetailById(Long id);
}
