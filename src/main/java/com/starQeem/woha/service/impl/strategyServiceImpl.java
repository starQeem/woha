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
import com.starQeem.woha.util.updateGradeUtils;
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
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("create_time desc");
        if (id.equals(0L)) { //查询我发布的攻略
            List<strategy> strategyList = strategyMapper.getUserWithStrategyWithStrategyType(Long.valueOf(user.getId()));
            return new PageInfo<>(strategyList, pageSize);
        } else { //查询用户发布的攻略
            List<strategy> strategyList = strategyMapper.getUserWithStrategyWithStrategyType(id);
            return new PageInfo<>(strategyList, pageSize);
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
        //查询数据库
        List<strategy> strategyList = strategyMapper.pageStrategyWithStrategyTypeById(id, title);
        //返回分页集合
        return new PageInfo<>(strategyList, pageSize);

    }

    /*
     * 攻略编辑回显
     * */
    @Override
    public strategy getUserWithStrategyWithStrategyTypeById(Long id) {
        return strategyMapper.getUserWithStrategyWithStrategyTypeById(id);
    }

    /*
     * 文章编辑
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
     * 文章详情
     * */
    @Override
    public strategy getStrategyDetailById(Long id) {
        //查询redis中的文章详情
        String getStrategyDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + id);

        if (StrUtil.isNotBlank(getStrategyDetail)) {
            //不为空,直接返回
            return processStrategyDetail(getStrategyDetail, id);
        } else if (getStrategyDetail != null) {
            return null;
        } else {
            //为空,查询数据库
            strategy strategy = strategyMapper.getStrategyDetailById(id);
            if (strategy == null) {
                //数据库中也没有,缓存空字符串
                stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将文章详情存入redis
            stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, JSONUtil.toJsonStr(strategy), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            return processStrategyDetail(JSONUtil.toJsonStr(strategy), id);
        }
    }
    /*
    * 登录后查询文章详情
    * */
    @Override
    public strategy queryStrategyDetailById(Long id, Long userId) {
        //查询redis中的文章详情
        String getStrategyDetail = stringRedisTemplate.opsForValue().get(STRATEGY_DETAIL + id);

        if (StrUtil.isNotBlank(getStrategyDetail)) {
            //不为空,直接返回
            return processStrategyDetail(getStrategyDetail, id, userId);
        } else if (getStrategyDetail != null) {
            return null;
        } else {
            //为空,查询数据库
            strategy strategy = strategyMapper.getStrategyDetailById(id);
            if (strategy == null) {
                //数据库中也没有,缓存空字符串
                stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将文章详情存入redis
            stringRedisTemplate.opsForValue().set(STRATEGY_DETAIL + id, JSONUtil.toJsonStr(strategy), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            return processStrategyDetail(JSONUtil.toJsonStr(strategy), id, userId);
        }
    }
    /*
    * 更新浏览次数和转换MarkDown格式
    * */
    private strategy processStrategyDetail(String strategyDetail, Long id) {
        strategy strategy = JSONUtil.toBean(strategyDetail, strategy.class);
        UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("views = views + 1").eq("id", id); //浏览次数+1
        strategyService.update(updateWrapper);
        //将文章内容转换为html格式
        String html = MarkdownUtil.markdownToHtml(strategy.getContent());
        strategy.setContent(html);
        return strategy;
    }
    /*
     * 更新浏览次数和转换MarkDown格式和更新任务状态
     * */
    private strategy processStrategyDetail(String strategyDetail, Long id, Long userId) {
        strategy strategy = JSONUtil.toBean(strategyDetail, strategy.class);
        UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("views = views + 1").eq("id", id); //浏览次数+1
        strategyService.update(updateWrapper);

        if (userId != null) {//判断用户是否为空
            //不为空,则已经登录,查询任务状态
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask != null && userTask.getDailytaskStrategy() == STATUS_ZERO) {
                //未完成,设置为完成并增加经验值
                userTask.setDailytaskStrategy(STATUS_ONE);
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);
                userTaskService.updateById(updateGradeUtils.updateGrade(userTask));
            }
        }
        //将文章内容转换为html格式
        String html = MarkdownUtil.markdownToHtml(strategy.getContent());
        strategy.setContent(html);
        return strategy;
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

        strategyComments.forEach(commentList -> likedUserIdsMap.entrySet().stream()//遍历所有点赞
                .filter(commentLiked -> commentList.getId().toString().equals(commentLiked.getKey())) //判断点赞的key和评论的id是否相等
                .findFirst()
                .ifPresent(commentLiked -> commentList.setLikedUser(commentLiked.getValue())));//相等则把点赞的用户赋值给评论对象

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
        //返回图片列表
        return strategyMapper.selectList(queryWrapper);

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
            return userService.getBaseMapper().selectList(queryWrapper);
        } else {
            return null;
        }
    }
}
