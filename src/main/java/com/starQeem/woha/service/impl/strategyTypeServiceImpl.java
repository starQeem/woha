package com.starQeem.woha.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starQeem.woha.mapper.strategyTypeMapper;
import com.starQeem.woha.pojo.strategyType;
import com.starQeem.woha.service.strategyTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.starQeem.woha.util.constant.STRATEGY_TYPE_LIST;
import static com.starQeem.woha.util.constant.TIME_SMALL;

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
            stringRedisTemplate.opsForValue().set(STRATEGY_TYPE_LIST,strategyTypeListJson,TIME_SMALL, TimeUnit.SECONDS);
            return strategyTypeList;
        }
    }
}
