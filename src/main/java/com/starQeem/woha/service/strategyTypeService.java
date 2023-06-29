package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.strategyType;

import java.util.List;

/**
 * @Date: 2023/4/22 19:45
 * @author: Qeem
 */
public interface strategyTypeService extends IService<strategyType> {
    /**
     * 查询文章的所有分类
     *
     * @return {@link List}<{@link strategyType}>
     */
    List<strategyType> queryStrategyType();

    /**
     * 获取文章类型列表
     *
     * @param pageNum 页面num
     * @return {@link PageInfo}<{@link strategyType}>
     */
    PageInfo<strategyType> getStrategyTypeList(Integer pageNum);

    /**
     * 保存文章类型
     *
     * @param strategyType 文章类型
     */
    void saveStrategyType(strategyType strategyType);

    /**
     * 通过id获取文章类型
     *
     * @param id id
     * @return {@link strategyType}
     */
    strategyType getStrategyTypeById(Long id);

    /**
     * 文章更新通过id类型
     *
     * @param strategyType 策略类型
     */
    void StrategyTypeUpdateById(strategyType strategyType);

    /**
     * 策略类型删除id
     *
     * @param id id
     */
    void StrategyTypeDeleteById(Long id);
}
