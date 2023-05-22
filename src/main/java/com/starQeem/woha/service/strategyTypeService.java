package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.strategyType;

import java.util.List;

/**
 * @Date: 2023/4/22 19:45
 * @author: Qeem
 */
public interface strategyTypeService extends IService<strategyType> {
    List<strategyType> queryStrategyType();
}
