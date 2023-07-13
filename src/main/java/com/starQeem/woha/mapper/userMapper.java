package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Date: 2023/4/17 0:01
 * @author: Qeem
 */
@Mapper
public interface userMapper extends BaseMapper<User> {
    User getUserWithGrade(Long id);
    List<User> getFollowUser(Long userId);
    List<User> getFansUser(Long userId);
    List<User> getUserList(@Param("nickName") String nickName);
}
