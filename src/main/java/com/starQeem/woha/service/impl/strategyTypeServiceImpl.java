package com.starQeem.woha.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.mapper.strategyTypeMapper;
import com.starQeem.woha.pojo.strategyType;
import com.starQeem.woha.service.strategyTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/22 19:46
 * @author: Qeem
 */
@Service
public class strategyTypeServiceImpl extends ServiceImpl<strategyTypeMapper, strategyType> implements strategyTypeService {
    @Resource
    private strategyTypeService strategyTypeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询策略类型
     *
     * @return {@link List}<{@link strategyType}>
     */
    @Override
    public List<strategyType> queryStrategyType() {
        //查询redis中有没有分类列表
        String getStrategyTypeList = stringRedisTemplate.opsForValue().get(STRATEGY_TYPE_LIST);
        if (StrUtil.isNotBlank(getStrategyTypeList)){
            //有列表,直接返回redis中查到的数据
            List<strategyType> strategyTypeList = JSONUtil.toList(JSONUtil.parseArray(getStrategyTypeList), strategyType.class);
            return strategyTypeList;
        }else {
            //没有列表,查询数据库
            List<strategyType> strategyTypeList = strategyTypeService.list();
            //将数据库中的列表信息存入redis中
            String strategyTypeListJson = JSONUtil.toJsonStr(strategyTypeList);
            stringRedisTemplate.opsForValue().set(STRATEGY_TYPE_LIST,strategyTypeListJson,TIME_STRATEGY, TimeUnit.SECONDS);
            return strategyTypeList;
        }
    }

    /**
     * 获取战略类型列表
     *
     * @param pageNum 页面num
     * @return {@link PageInfo}<{@link strategyType}>
     */
    @Override
    public PageInfo<strategyType> getStrategyTypeList(Integer pageNum) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        PageHelper.startPage(pageNum,PAGE_SIZE);
        PageHelper.orderBy("id desc");
        List<strategyType> strategyTypeList = strategyTypeService.list();
        return new PageInfo<>(strategyTypeList);
    }

    /**
     * 拯救策略类型
     *
     * @param strategyType 策略类型
     */
    @Override
    public void saveStrategyType(strategyType strategyType) {
        boolean isSuccess = strategyTypeService.save(strategyType);
        if (isSuccess){
            //查询redis中有没有分类列表
            String getStrategyTypeList = stringRedisTemplate.opsForValue().get(STRATEGY_TYPE_LIST);
            if (getStrategyTypeList!=null){
                stringRedisTemplate.delete(STRATEGY_TYPE_LIST); //删除攻略分类列表缓存
            }
        }
    }

    /**
     * 通过id获取攻略分类
     *
     * @param id id
     * @return {@link strategyType}
     */
    @Override
    public strategyType getStrategyTypeById(Long id) {
        QueryWrapper<strategyType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return strategyTypeService.getBaseMapper().selectOne(queryWrapper);
    }

    /**
     * 策略更新通过id类型
     *
     * @param strategyType 策略类型
     */
    @Override
    public void StrategyTypeUpdateById(strategyType strategyType) {
        boolean isSuccess = strategyTypeService.updateById(strategyType);
        if (isSuccess){
            //查询redis中有没有分类列表
            String getStrategyTypeList = stringRedisTemplate.opsForValue().get(STRATEGY_TYPE_LIST);
            if (getStrategyTypeList!=null){
                stringRedisTemplate.delete(STRATEGY_TYPE_LIST); //删除攻略分类列表缓存
            }
        }
    }

    /**
     * 策略类型删除id
     *
     * @param id id
     */
    @Override
    public void StrategyTypeDeleteById(Long id) {
        boolean isSuccess = strategyTypeService.removeById(id);
        if (isSuccess){
            //查询redis中有没有分类列表
            String getStrategyTypeList = stringRedisTemplate.opsForValue().get(STRATEGY_TYPE_LIST);
            if (getStrategyTypeList!=null){
                stringRedisTemplate.delete(STRATEGY_TYPE_LIST); //删除攻略分类列表缓存
            }
        }
    }
}
