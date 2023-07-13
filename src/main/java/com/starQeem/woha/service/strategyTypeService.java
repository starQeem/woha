package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.StrategyType;

import java.util.List;

/**
 * @Date: 2023/4/22 19:45
 * @author: Qeem
 */
public interface strategyTypeService extends IService<StrategyType> {
    /**
     * 查询文章的所有分类
     *
     * @return {@link List}<{@link StrategyType}>
     */
    List<StrategyType> queryStrategyType();

    /**
     * 获取文章类型列表
     *
     * @param pageNum 页面num
     * @return {@link PageInfo}<{@link StrategyType}>
     */
    PageInfo<StrategyType> getStrategyTypeList(Integer pageNum);

    /**
     * 保存文章类型
     *
     * @param strategyType 文章类型
     */
    void saveStrategyType(StrategyType strategyType);

    /**
     * 通过id获取文章类型
     *
     * @param id id
     * @return {@link StrategyType}
     */
    StrategyType getStrategyTypeById(Long id);

    /**
     * 文章更新通过id类型
     *
     * @param strategyType 策略类型
     */
    void StrategyTypeUpdateById(StrategyType strategyType);

    /**
     * 策略类型删除id
     *
     * @param id id
     */
    void StrategyTypeDeleteById(Long id);
}
