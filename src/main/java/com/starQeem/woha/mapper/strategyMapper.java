package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.Strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/22 14:33
 * @author: Qeem
 */
@Mapper
public interface strategyMapper extends BaseMapper<Strategy> {
    List<Strategy> getUserWithStrategyWithStrategyType(Long userId);
    Strategy getUserWithStrategyWithStrategyTypeById(Long id);
    List<Strategy> pageStrategyWithStrategyTypeById(Long id, String title);
    Strategy getStrategyDetailById(Long id);
}
