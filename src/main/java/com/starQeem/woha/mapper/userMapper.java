package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.user;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/17 0:01
 * @author: Qeem
 */
@Mapper
public interface userMapper extends BaseMapper<user> {
    user getUserWithGrade(Long id);
    List<user> getFollowUser(Long userId);
    List<user> getFansUser(Long userId);
}
