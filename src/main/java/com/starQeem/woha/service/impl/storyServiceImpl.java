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
import com.starQeem.woha.mapper.storyMapper;
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
 * @Date: 2023/4/17 15:43
 * @author: Qeem
 */
@Service
public class storyServiceImpl extends ServiceImpl<storyMapper, story> implements storyService {
    @Resource
    private storyService storyService;
    @Resource
    private storyMapper storyMapper;
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
     * 新增我的故事
     * */
    @Override
    public boolean saveStory(story story) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        story.setCreateTime(new Date());
        story.setUpdateTime(new Date());
        story.setViews(0);
        story.setUserId(Long.valueOf(user.getId()));
        return storyService.save(story);
    }

    /*
     * 根据用户id查询故事列表
     * */
    @Override
    public PageInfo<story> queryMyStory(Integer pageNum, int PAGE_SIZE, Long id) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        PageHelper.startPage(pageNum, PAGE_SIZE);
        PageHelper.orderBy("create_time desc");
        if (id.equals(0L)) {
            List<story> storyList = storyMapper.getUserWithStory(Long.valueOf(user.getId()));
            return new PageInfo<>(storyList, PAGE_SIZE);
        } else {
            List<story> storyList = storyMapper.getUserWithStory(id);
            return new PageInfo<>(storyList, PAGE_SIZE);
        }
    }

    /*
     * 查询故事列表
     * */
    @Override
    public PageInfo<story> getStoryListPageInfo(Integer pageNum, int pageSize, String title) {
        if (pageNum == null) {
            pageNum = PAGE_NUM;
        }
        if (title == null) {
            title = "";
        } else {
            pageSize = SEARCH_SIZE;
        }
        PageHelper.startPage(pageNum, pageSize);
        //查询数据库
        List<story> storyList = storyMapper.getStory(title);
        return new PageInfo<>(storyList, pageSize);
    }

    /*
     * 更新我的故事
     * */
    @Override
    public boolean updateStory(story story) {
        story.setUpdateTime(new Date());
        story.setCreateTime(new Date());
        boolean isSuccess = storyService.updateById(story);
        //删除故事缓存
        String redisStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + story.getId());
        if (StrUtil.isNotBlank(redisStoryDetail)) {
            stringRedisTemplate.delete(STORY_DETAIL + story.getId());
        }
        return isSuccess;
    }

    /*
    * 登录后查看问答详情
    * */
    @Override
    public story queryStoryDetail(Long id) {
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        //查询redis中的问答详情
        String getStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + id);

        if (StrUtil.isNotBlank(getStoryDetail)) {
            //不为空,直接返回
            return processStoryDetail(getStoryDetail, id, user);
        } else if (getStoryDetail != null) {
            return null;
        } else {
            //为空,查询数据库
            story story = storyMapper.getStoryById(id);
            if (story == null) { //判断数据库中查询出的结果是否为空
                //为空,缓存空字符串
                stringRedisTemplate.opsForValue().set(STORY_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将问答详情存入redis
            stringRedisTemplate.opsForValue().set(STORY_DETAIL + id, JSONUtil.toJsonStr(story), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            return processStoryDetail(JSONUtil.toJsonStr(story), id, user);
        }
    }
    /*
    * 查看问答详情
    * */
    @Override
    public story getStoryById(Long id) {
        //查询redis中的问答详情
        String getStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + id.toString());

        if (StrUtil.isNotBlank(getStoryDetail)) {
            //不为空,直接返回
            return processStoryDetail(getStoryDetail, id, null);
        } else if (getStoryDetail != null) {
            return null;
        } else {
            //为空,查询数据库
            story story = storyMapper.getStoryById(id);
            if (story == null) {//判断数据库中查询出的结果是否为空
                //为空,缓存空字符串
                stringRedisTemplate.opsForValue().set(STORY_DETAIL + id,"", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将问答详情存入redis
            stringRedisTemplate.opsForValue().set(STORY_DETAIL + id, JSONUtil.toJsonStr(story), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            return processStoryDetail(JSONUtil.toJsonStr(story), id, null);
        }
    }
    /*
    * 更新浏览次数和转换Markdown格式
    * */
    private story processStoryDetail(String storyDetail, Long id, userDto user) {
        story story = JSONUtil.toBean(storyDetail, story.class);//将string类型转换为story类型
        UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("views = views + 1").eq("id", id);//浏览次数+1
        storyService.update(updateWrapper);

        if (user != null) { //判断用户是否为空
            //不为空,查询用户每日任务情况
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", Long.valueOf(user.getId()));
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask.getDailytaskStory() == STATUS_ZERO) { //判断任务有没有完成
                //未完成.设置为完成并增加经验
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);
                userTask.setDailytaskStory(STATUS_ONE);
                userTaskService.updateById(updateGradeUtils.updateGrade(userTask));
            }
        }
        //将文章内容转换为html格式
        String html = MarkdownUtil.markdownToHtml(story.getContent());
        story.setContent(html);
        return story;
    }

    /*
     * 查询评论区
     * */
    @Override
    public List<comment> getComments(Long id) {
        //查询所有评论
        List<comment> storyComments = commentMapper.getStoryComments(id);
        //查询所有点赞
        Object likedUserIds = stringRedisTemplate.opsForHash().entries(COMMENT_LIKED);
        // 创建一个Map来存储likedUserIds的键值对
        Map<String, String> likedUserIdsMap;
        likedUserIdsMap = (Map<String, String>) likedUserIds;

        storyComments.forEach(commentList -> likedUserIdsMap.entrySet().stream()//遍历所有点赞
                .filter(commentLiked -> commentList.getId().toString().equals(commentLiked.getKey())) //判断点赞的key和评论的id是否相等
                .findFirst()
                .ifPresent(commentLiked -> commentList.setLikedUser(commentLiked.getValue())));//相等则把点赞的用户赋值给评论对象
        
        return storyComments;
    }

    /*
     * 文章点赞
     * */
    @Override
    public boolean liked(Long storyId, Long userId) {
        //判断是否点过赞
        Object getStory = stringRedisTemplate.opsForZSet().score(STORY_LIKED + storyId, String.valueOf(userId));
        if (getStory != null) {//点过赞
            //删除点赞信息
            stringRedisTemplate.opsForZSet().remove(STORY_LIKED + storyId, String.valueOf(userId));
            return true;
        } else {
            //没点过赞
            stringRedisTemplate.opsForZSet().add(STORY_LIKED + storyId, String.valueOf(userId), new Date().getTime());
            return false;
        }
    }

    /*
     * 查询是否点赞
     * */
    @Override
    public boolean getStatus(Long storyId) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        Object getStory = stringRedisTemplate.opsForZSet().score(STORY_LIKED + storyId.toString(), user.getId().toString());
        if (getStory == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * 查询五条故事(按照时间降序)
     * */
    @Override
    public List<story> getStoryListFive() {
        //查询数据库
        QueryWrapper<story> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "update_time").
                orderByDesc("update_time").
                orderByDesc("liked").
                last("limit 5");
        //返回图片列表
        return storyMapper.selectList(queryWrapper);

    }

    /*
     * 故事删除
     * */
    @Override
    @Transactional
    public boolean removeStoryById(Long id) {
        storyService.removeById(id);
        //删除关联的评论信息
        QueryWrapper<comment> queryWrapperComment = new QueryWrapper<>();
        queryWrapperComment.eq("story_id", id);
        commentService.remove(queryWrapperComment);
        //删除故事缓存
        String redisStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + id);
        if (StrUtil.isNotBlank(redisStoryDetail)) {
            stringRedisTemplate.delete(STORY_DETAIL + id);
        }
        //删除故事的点赞信息
        Object redisStoryLikedUser = stringRedisTemplate.opsForZSet().range(STORY_LIKED + id, 0, -1);
        if (redisStoryLikedUser != null) {
            stringRedisTemplate.delete(STORY_LIKED + id);
        }
        return false;
    }

    /*
     * 查询点赞数
     * */
    @Override
    public Integer getLikedCount(Long id) {
        int liked = stringRedisTemplate.opsForZSet().size(STORY_LIKED + id.toString()).intValue();
        story story = new story();
        story.setId(id);
        story.setLiked(liked);
        storyService.updateById(story);
        return liked;
    }

    /*
     * 获取点赞的前三名用户
     * */
    @Override
    public List<user> getLikedUserThree(Long id) {
        Set<String> range = stringRedisTemplate.opsForZSet().range(STORY_LIKED + id.toString(), 0, 2);
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
