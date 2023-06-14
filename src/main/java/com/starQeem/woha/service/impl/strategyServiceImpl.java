package com.starQeem.woha.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.mapper.strategyMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
import com.starQeem.woha.util.MarkdownUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/22 14:35
 * @author: Qeem
 */
@Service
public class strategyServiceImpl extends ServiceImpl<strategyMapper, strategy> implements strategyService {
    @Resource
    private strategyService strategyService;
    @Resource
    private strategyMapper strategyMapper;
    @Resource
    private userTaskService userTaskService;
    @Resource
    private commentMapper commentMapper;
    @Resource
    private commentService commentService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private userService userService;

    /*
     * 新增攻略
     * */
    @Override
    public boolean saveStrategy(strategy strategy) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        strategy.setViews(0);
        strategy.setUpdateTime(new Date());
        strategy.setCreateTime(new Date());
        strategy.setUserId(Long.valueOf(user.getId()));
        return strategyService.save(strategy);
    }

    /*
     * 查询我发布的攻略
     * */
    @Override
    public PageInfo<strategy> getUserWithStrategyWithStrategyType(Integer pageNum, int pageSize, Long id) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("create_time desc");
        if (id.equals(0L)) { //查询我发布的攻略
            List<strategy> strategyList = strategyMapper.getUserWithStrategyWithStrategyType(Long.valueOf(user.getId()));
            PageInfo<strategy> pageInfo = new PageInfo<>(strategyList, pageSize);
            return pageInfo;
        } else { //查询用户发布的攻略
            List<strategy> strategyList = strategyMapper.getUserWithStrategyWithStrategyType(id);
            PageInfo<strategy> pageInfo = new PageInfo<>(strategyList, pageSize);
            return pageInfo;
        }
    }

    /*
     * 查询攻略分类列表
     * */
    @Override
    public PageInfo<strategy> pageStrategyWithStrategyTypeById(Integer pageNum, int pageSize, Long id, String title) {
        if (title == null) {
            title = "";
        } else {
            pageSize = SEARCH_SIZE;
        }
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("liked desc");
        PageHelper.orderBy("create_time desc");
        //查询数据库
        List<strategy> strategyList = strategyMapper.pageStrategyWithStrategyTypeById(id, title);
        PageInfo<strategy> pageInfo = new PageInfo<>(strategyList, pageSize);
        //返回分页集合
        return pageInfo;

    }

    /*
     * 攻略编辑回显
     * */
    @Override
    public strategy getUserWithStrategyWithStrategyTypeById(Long id) {
        return strategyMapper.getUserWithStrategyWithStrategyTypeById(id);
    }

    /*
     * 百科编辑
     * */
    @Override
    public boolean updateStrategy(strategy strategy) {
        strategy.setUpdateTime(new Date());
        strategy.setCreateTime(new Date());
        boolean isSuccess = strategyService.updateById(strategy);
        //删除缓存
        String redisStrategyDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + strategy.getId());
        if (StrUtil.isNotBlank(redisStrategyDetail)) {
            stringRedisTemplate.delete(STRATEGY_DETAIL + strategy.getId());
        }
        return isSuccess;
    }

    /*
     * 攻略详情
     * */
    @Override
    public strategy getStrategyDetailById(Long id) {
        //查询redis中的故事详情
        String getStrategyDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + id);
        if (StrUtil.isNotBlank(getStrategyDetail)) {
            //不为空,直接返回
            strategy strategy = JSONUtil.toBean(getStrategyDetail, strategy.class);
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            strategyService.update(updateWrapper);
            String html = MarkdownUtil.markdownToHtml(strategy.getContent());
            strategy.setContent(html);
            return strategy;
        } else if (getStrategyDetail != null) {
            return null;
        } else {
            //从数据库中查询故事详情
            strategy strategy = strategyMapper.getStrategyDetailById(id);
            if (strategy == null) {
                //缓存空字符串
                stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将数据库中查询的故事详情写入redis中
            stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, JSONUtil.toJsonStr(strategy), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            strategyService.update(updateWrapper);
            String html = MarkdownUtil.markdownToHtml(strategy.getContent());
            strategy.setContent(html);
            //返回故事详情
            return strategy;
        }
    }

    /*
     * 登录后查询攻略详情
     * */
    @Override
    public strategy queryStrategyDetailById(Long id, Long userId) {
        //查询redis中的攻略详情
        String getStrategyDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + id);
        if (StrUtil.isNotBlank(getStrategyDetail)) {
            //不为空,直接返回
            strategy strategy = JSONUtil.toBean(getStrategyDetail, strategy.class);
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            strategyService.update(updateWrapper);
            //我的任务
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask != null && userTask.getDailytaskStrategy() == STATUS_ZERO) {  //判断每日任务是否未完成
                userTask.setDailytaskStrategy(STATUS_ONE);  //设置为已经完成状态
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);  //经验+
                if (userTask.getExperience() >= GRADE_SIX) {
                    userTask.setGrade(6);
                } else {
                    switch (userTask.getExperience() / 100) {
                        case 9:
                        case 8:
                        case 7:
                            userTask.setGrade(5);
                            break;
                        case 6:
                        case 5:
                            userTask.setGrade(4);
                            break;
                        case 4:
                            userTask.setGrade(3);
                            break;
                        case 3:
                            userTask.setGrade(2);
                            break;
                        default:
                            userTask.setGrade(1);
                    }
                }
                userTaskService.updateById(userTask);
            }
            String html = MarkdownUtil.markdownToHtml(strategy.getContent());
            strategy.setContent(html);
            return strategy;
        } else if (getStrategyDetail != null) {
            return null;
        } else {
            //从数据库中查询攻略详情
            strategy strategy = strategyMapper.getStrategyDetailById(id);
            if (strategy == null) {
                //缓存空字符串
                stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            strategyService.update(updateWrapper);
            //将数据库中的攻略详情写入redis中
            stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, JSONUtil.toJsonStr(strategy), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            //我的任务
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask != null && userTask.getDailytaskStrategy() == STATUS_ZERO) {  //判断每日任务是否未完成
                userTask.setDailytaskStrategy(STATUS_ONE);  //设置为已经完成状态
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);  //经验+
                if (userTask.getExperience() >= GRADE_SIX) {
                    userTask.setGrade(6);
                } else {
                    switch (userTask.getExperience() / 100) {
                        case 9:
                        case 8:
                        case 7:
                            userTask.setGrade(5);
                            break;
                        case 6:
                        case 5:
                            userTask.setGrade(4);
                            break;
                        case 4:
                            userTask.setGrade(3);
                            break;
                        case 3:
                            userTask.setGrade(2);
                            break;
                        default:
                            userTask.setGrade(1);
                    }
                }
                userTaskService.updateById(userTask);
            }
            String html = MarkdownUtil.markdownToHtml(strategy.getContent());
            strategy.setContent(html);
            return strategy;
        }
    }

    /*
     * 查询评论区
     * */
    @Override
    public List<comment> getComments(Long id) {
        //查询所有评论
        List<comment> strategyComments = commentMapper.getStrategyComments(id);
        //查询所有点赞
        Object likedUserIds = stringRedisTemplate.opsForHash().entries(COMMENT_LIKED);
        // 创建一个Map来存储likedUserIds的键值对
        Map<String, String> likedUserIdsMap;
        likedUserIdsMap = (Map<String, String>) likedUserIds;
        for (comment commentList : strategyComments) {  //遍历所有评论
            for (Map.Entry<String, String> commentLiked : likedUserIdsMap.entrySet()) {  //遍历所有点赞
                if (commentList.getId().toString().equals(commentLiked.getKey())) {  //判断点赞的key和评论的id是否相等
                    commentList.setLikedUser(commentLiked.getValue());  //相等则把点赞的用户赋值给评论对象
                }
            }
        }
        return strategyComments;
    }

    /*
     * 攻略点赞
     * */
    @Override
    public boolean liked(Long strategyId, Long userId) {
        //判断是否点过赞
        Object getStrategy = stringRedisTemplate.opsForZSet().score(STRATEGY_LIKED + strategyId, String.valueOf(userId));
        if (getStrategy != null) {//点过赞
            //删除点赞信息
            stringRedisTemplate.opsForZSet().remove(STRATEGY_LIKED + strategyId, String.valueOf(userId));
            return true;
        } else {
            //没点过赞
            stringRedisTemplate.opsForZSet().add(STRATEGY_LIKED + strategyId, String.valueOf(userId), new Date().getTime());
            return false;
        }
    }

    /*
     * 查询是否点赞
     * */
    @Override
    public boolean getStatus(Long strategyId) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Object getStrategy = stringRedisTemplate.opsForZSet().score(STRATEGY_LIKED + strategyId.toString(), user.getId().toString());
        if (getStrategy == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * 查询五条攻略记录(按照更新时间降序)
     * */
    @Override
    public List<strategy> getStrategyListFive() {
        //查询数据库
        QueryWrapper<strategy> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "update_time")
                .orderByDesc("update_time")
                .orderByDesc("liked")
                .last("limit 5");
        List<strategy> strategyList = strategyMapper.selectList(queryWrapper);
        //返回图片列表
        return strategyList;

    }

    /*
     * 删除攻略
     * */
    @Override
    @Transactional
    public boolean removeStrategyById(Long id) {
        strategyService.removeById(id);
        //删除关联的评论信息
        QueryWrapper<comment> queryWrapperComment = new QueryWrapper<>();
        queryWrapperComment.eq("strategy_id", id);
        commentService.remove(queryWrapperComment);
        //删除缓存
        String redisStrateDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + id);
        if (StrUtil.isNotBlank(redisStrateDetail)) {
            stringRedisTemplate.delete(STRATEGY_DETAIL + id);
        }
        //删除攻略的点赞信息
        Object redisStrategyLikedUser = stringRedisTemplate.opsForZSet().range(STRATEGY_LIKED + id, 0, -1);
        if (redisStrategyLikedUser != null) {
            stringRedisTemplate.delete(STRATEGY_LIKED + id);
        }
        return false;
    }

    /*
     * 查询点赞数
     * */
    @Override
    public Integer getLikedCount(Long id) {
        int liked = stringRedisTemplate.opsForZSet().size(STRATEGY_LIKED + id.toString()).intValue();
        strategy strategy = new strategy();
        strategy.setId(id);
        strategy.setLiked(liked);
        strategyService.updateById(strategy);
        return liked;
    }

    /*
     * 获取点赞的前三名用户
     * */
    @Override
    public List<user> getLikedUserThree(Long id) {
        Set<String> range = stringRedisTemplate.opsForZSet().range(STRATEGY_LIKED + id.toString(), 0, 2);
        if (!range.isEmpty()) {
            String firstThree = String.join(",", range);
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "avatar")
                    .apply("FIND_IN_SET(id, {0})", firstThree)
                    .last("ORDER BY FIELD(id, " + firstThree + ")");
            List<user> ThreeUserLikedList = userService.getBaseMapper().selectList(queryWrapper);
            return ThreeUserLikedList;
        } else {
            return null;
        }
    }
}
