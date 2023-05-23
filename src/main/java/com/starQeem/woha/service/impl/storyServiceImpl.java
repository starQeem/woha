package com.starQeem.woha.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.mapper.storyMapper;
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
    public PageInfo<story> queryMyStory(Integer pageNum, int PAGE_SIZE,Long id) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        PageHelper.startPage(pageNum, PAGE_SIZE);
        PageHelper.orderBy("create_time desc");
        if (id.equals(0L)){
            List<story> storyList = storyMapper.getUserWithStory(Long.valueOf(user.getId()));
            PageInfo<story> pageInfo = new PageInfo<>(storyList, PAGE_SIZE);
            return pageInfo;
        }else {
            List<story> storyList = storyMapper.getUserWithStory(id);
            PageInfo<story> pageInfo = new PageInfo<>(storyList, PAGE_SIZE);
            return pageInfo;
        }
    }

    /*
     * 查询故事列表
     * */
    @Override
    public PageInfo<story> getStoryListPageInfo(Integer pageNum, int pageSize, String title) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("liked desc");
        PageHelper.orderBy("create_time desc");
        //查询redis中有没有列表
        String getStoryListJson = stringRedisTemplate.opsForValue().get(STORY_LIST + pageNum);
        if (StrUtil.isNotBlank(getStoryListJson) && StrUtil.isBlank(title)) {//判断查询出来的数据是否不为空且title为空（title不为空时为搜索，搜索直接走数据库）
            //有列表,直接返回redis中查到的数据
            JSONObject jsonObj = JSONUtil.parseObj(getStoryListJson);
            // 将JSONObject对象转换为PageInfo<Pictures>对象
            PageInfo<story> pageInfo = (PageInfo<story>) jsonObj.toBean(PageInfo.class);
            //返回分页集合
            return pageInfo;
        } else {
            //没有列表,查询数据库
            List<story> storyList = storyMapper.getStory(title);
            //将数据库中的列表信息存入redis中
            PageInfo<story> pageInfo = new PageInfo<>(storyList, pageSize);
            if (StrUtil.isBlank(title)){
                JSONObject jsonObj = JSONUtil.parseObj(pageInfo);
                // 将JSONObject对象转换为PageInfo<Story>对象
                PageInfo<story> storyListPageInfo = (PageInfo<story>) jsonObj.toBean(PageInfo.class);
                String storyListJson = JSONUtil.toJsonStr(storyListPageInfo);
                stringRedisTemplate.opsForValue().set(STORY_LIST + pageNum, storyListJson, TIME_SMALL, TimeUnit.SECONDS);
            }
            //将List集合丢到分页对象里
            return pageInfo;
        }
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
        if (StrUtil.isNotBlank(redisStoryDetail)){
            stringRedisTemplate.delete(STORY_DETAIL + story.getId());
        }
        return isSuccess;
    }

    /*
     * 查询最近更新的三条我的故事记录
     * */
    @Override
    public List<story> getMyPicturesIndexByUpdateTime(Long userId) {
        return storyMapper.getMyPicturesIndexByUpdateTime(userId);
    }

    /*
     * 登录后查询故事详情
     * */
    @Override
    public story queryStoryDetail(Long id) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        //查询redis中的故事详情
        String getStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + id);
        if (StrUtil.isNotBlank(getStoryDetail)) {
            //不为空,直接返回
            story story = JSONUtil.toBean(getStoryDetail, story.class);
            QueryWrapper<story> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.select("id","views").eq("id",id);
            story story2 = storyMapper.selectOne(queryWrapper2);
            Integer views = story2.getViews();
            story2.setViews(views + 1);
            getBaseMapper().updateById(story2);  //浏览次数+1
            //我的任务
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", Long.valueOf(user.getId()));
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask.getDailytaskStory() == STATUS_ZERO) {  //判断每日任务是否未完成
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);  //经验+
                userTask.setDailytaskStory(STATUS_ONE);  //设置为完成状态
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
            String html = MarkdownUtil.markdownToHtml(story.getContent());
            story.setContent(html);
            return story;
        }else if (getStoryDetail != null){
            return null;
        }else {
            //从数据库中查询故事详情
            story story = storyMapper.getStoryById(id);
            if (story == null){
                stringRedisTemplate.opsForValue().set(STORY_DETAIL + id,"",TIME_BIG,TimeUnit.SECONDS);
                return null;
            }
            Integer views = story.getViews();
            story.setViews(views + 1);
            getBaseMapper().updateById(story);  //浏览次数+1
            //将数据库中查询的故事详情写入redis中
            stringRedisTemplate.opsForValue().set(STORY_DETAIL + id, JSONUtil.toJsonStr(story), TIME_MAX + RandomUtil.randomInt(0,300), TimeUnit.SECONDS);
            //我的任务
            QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", Long.valueOf(user.getId()));
            userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
            if (userTask.getDailytaskStory() == STATUS_ZERO) {  //判断每日任务是否未完成
                Integer experience = userTask.getExperience();
                userTask.setExperience(experience + TASK_DAY_EXPERIENCE);  //经验+
                userTask.setDailytaskStory(STATUS_ONE);  //设置为完成状态
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
            String html = MarkdownUtil.markdownToHtml(story.getContent());
            story.setContent(html);
            return story;
        }
    }

    /*
     * 查询故事详情
     * */
    @Override
    public story getStoryById(Long id) {
        //查询redis中的故事详情
        String getStoryDetail = stringRedisTemplate.opsForValue().get(STORY_DETAIL + id.toString());
        if (StrUtil.isNotBlank(getStoryDetail)) {
            //不为空,直接返回
            story story = JSONUtil.toBean(getStoryDetail, story.class);
            QueryWrapper<story> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.select("id","views").eq("id",id);
            story story2 = storyMapper.selectOne(queryWrapper2);
            Integer views = story2.getViews();
            story2.setViews(views + 1);
            getBaseMapper().updateById(story2);  //浏览次数+1
            String html = MarkdownUtil.markdownToHtml(story.getContent());
            story.setContent(html);
            return story;
        }else if (getStoryDetail != null){
            return null;
        }else {
            //从数据库中查询故事详情
            story story = storyMapper.getStoryById(id);
            if (story == null){
                //缓存空字符串
                stringRedisTemplate.opsForValue().set(STORY_DETAIL + id.toString(),"",TIME_BIG,TimeUnit.SECONDS);
                return null;
            }
            Integer views = story.getViews();
            story.setViews(views + 1);
            storyService.updateById(story);
            //将数据库中查询的故事详情写入redis中
            stringRedisTemplate.opsForValue().set(STORY_DETAIL + id.toString(), JSONUtil.toJsonStr(story), TIME_MAX+ RandomUtil.randomInt(0,300), TimeUnit.SECONDS);
            String html = MarkdownUtil.markdownToHtml(story.getContent());
            story.setContent(html);
            //返回故事详情
            return story;
        }
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
        for (comment commentList : storyComments){  //遍历所有评论
            for (Map.Entry<String, String> commentLiked : likedUserIdsMap.entrySet()){  //遍历所有点赞
                if (commentList.getId().toString().equals(commentLiked.getKey())){  //判断点赞的key和评论的id是否相等
                    commentList.setLikedUser(commentLiked.getValue());  //相等则把点赞的用户赋值给评论对象
                }
            }
        }
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
        //查询redis中是否存在故事列表
        String getStoryFiveList = stringRedisTemplate.opsForValue().get(STORY_FIVE_LIST);
        if (StrUtil.isNotBlank(getStoryFiveList)) {
            //存在,直接返回
            List<story> storyList = JSONUtil.toList(JSONUtil.parseArray(getStoryFiveList), story.class);
            return storyList;
        } else {
            //不存在,查询数据库
            QueryWrapper<story> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "title", "update_time").
                    orderByDesc("update_time").
                    orderByDesc("liked").
                    last("limit 5");
            List<story> storyList = storyMapper.selectList(queryWrapper);
            //将查询出的数据存入redis中
            stringRedisTemplate.opsForValue().set(STORY_FIVE_LIST, JSONUtil.toJsonStr(storyList), TIME_SMALL, TimeUnit.SECONDS);
            //返回图片列表
            return storyList;
        }
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
        if (StrUtil.isNotBlank(redisStoryDetail)){
            stringRedisTemplate.delete(STORY_DETAIL + id);
        }
        //删除故事的点赞信息
        Object redisStoryLikedUser = stringRedisTemplate.opsForZSet().range(STORY_LIKED + id,0,-1);
        if (redisStoryLikedUser != null){
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
        if (!range.isEmpty()){
            String firstThree = String.join(",", range);
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "avatar")
                    .apply("FIND_IN_SET(id, {0})", firstThree)
                    .last("ORDER BY FIELD(id, " + firstThree + ")");
            List<user> ThreeUserLikedList = userService.getBaseMapper().selectList(queryWrapper);
            return ThreeUserLikedList;
        }else {
            return null;
        }
    }
}
