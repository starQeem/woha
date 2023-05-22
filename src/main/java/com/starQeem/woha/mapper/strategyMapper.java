package com.starQeem.woha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starQeem.woha.pojo.strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: 2023/4/22 14:33
 * @author: Qeem
 */
@Mapper
public interface strategyMapper extends BaseMapper<strategy> {
    List<strategy> getUserWithStrategyWithStrategyType( Long userId);
    strategy getUserWithStrategyWithStrategyTypeById(Long id);
    List<strategy> pageStrategyWithStrategyTypeById(Long id, String title);
    strategy getStrategyDetailById(Long id);
}
