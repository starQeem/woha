package com.starQeem.woha.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.mapper.picturesMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
import com.starQeem.woha.util.updateGradeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/18 10:51
 * @author: Qeem
 */
@Service
public class picturesServiceImpl extends ServiceImpl<picturesMapper, pictures> implements picturesService {
    @Resource
    private picturesService picturesService;
    @Resource
    private picturesMapper picturesMapper;
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
     * 查询我的图片列表
     * */
    @Override
    public PageInfo<pictures> queryPictures(Integer pageNum, int pageSize) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("create_time desc");
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        List<pictures> picturesList = picturesMapper.getUserWithPictures(Long.valueOf(user.getId()));
        return new PageInfo<>(picturesList, pageSize);
    }

    /*
     * 查询图片列表
     * */
    @Override
    public PageInfo<pictures> getPicturesListPageInfo(Integer pageNum, int pageSize, String title) {
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
        List<pictures> picturesList = picturesMapper.getPicturesListPageInfo(title);
        return new PageInfo<>(picturesList, pageSize);
    }

    /*
     * 新增我的图片
     * */
    @Override
    public boolean savePictures(pictures pictures) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        pictures.setCreateTime(new Date());
        pictures.setUpdateTime(new Date());
        pictures.setViews(0);
        pictures.setUserId(Long.valueOf(user.getId()));
        //每周任务
        QueryWrapper<userTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", Long.valueOf(user.getId()));
        userTask userTask = userTaskService.getBaseMapper().selectOne(queryWrapper);
        if (userTask.getWeeklytaskPictures() == STATUS_ZERO) { //判断每周任务是否未完成
            Integer experience = userTask.getExperience(); //未完成
            userTask.setExperience(experience + TASK_WEEK_EXPERIENCE);
            userTask.setWeeklytaskPictures(STATUS_ONE);   //设置为已完成状态
            userTaskService.updateById(updateGradeUtils.updateGrade(userTask));//更新等级
        }
        return picturesService.save(pictures);
    }

    /*
     * 修改我的图片
     * */
    @Override
    public boolean updatePictures(pictures pictures) {
        pictures.setCreateTime(new Date());
        pictures.setUpdateTime(new Date());
        boolean isSuccess = picturesService.updateById(pictures);
        String redisPicturesDetail = stringRedisTemplate.opsForValue().get(PICTURES_DETAIL + pictures.getId());
        if (StrUtil.isNotBlank(redisPicturesDetail)) {
            stringRedisTemplate.delete(PICTURES_DETAIL + pictures.getId());
        }
        return isSuccess;
    }

    /*
     * 查询我的图片详情
     * */
    @Override
    public pictures queryPicturesDetail(Long id) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        pictures pictures = picturesMapper.queryPicturesWithUserById(id, Long.valueOf(user.getId()));
        UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("views = views + 1").eq("id", id);
        picturesService.update(updateWrapper);
        return pictures;
    }

    /*
     * 查询图片详情
     * */
    @Override
    public pictures getPicturesDetailById(Long id) {
        //查询redis中的图片详情
        String getPicturesDetail = stringRedisTemplate.opsForValue().get(PICTURES_DETAIL + id);
        if (StrUtil.isNotBlank(getPicturesDetail)) {
            //不为空,直接返回
            pictures pictures = JSONUtil.toBean(getPicturesDetail, pictures.class);
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            picturesService.update(updateWrapper);
            //返回图片详情
            return pictures;
        } else if (getPicturesDetail != null) {
            return null;
        } else {
            //为空,从数据库中查询图片详情
            pictures pictures = picturesMapper.getPicturesDetailById(id);
            if (pictures == null) {
                //缓存空字符串
                stringRedisTemplate.opsForValue().set(PICTURES_DETAIL + id, "", TIME_BIG, TimeUnit.SECONDS);
                return null;
            }
            //将数据库中查询出的图片详情写入redis中
            stringRedisTemplate.opsForValue().set(PICTURES_DETAIL + id, JSONUtil.toJsonStr(pictures), TIME_MAX + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("views = views + 1").eq("id", id);
            picturesService.update(updateWrapper);
            //返回图片详情
            return pictures;
        }
    }

    /*
     * 查询用户发布图片记录
     * */
    @Override
    public List<pictures> getUserPicturesWithUpdateTime(Long id) {
        return picturesMapper.getMyPicturesIndexByUpdateTime(id);
    }
    /*
     * 查询评论区
     * */
    @Override
    public List<comment> getComments(Long id) {
        //查询所有评论
        List<comment> picturesComments = commentMapper.getPicturesComments(id);
        //查询所有点赞
        Object likedUserIds = stringRedisTemplate.opsForHash().entries(COMMENT_LIKED);
        // 创建一个Map来存储likedUserIds的键值对
        Map<String, String> likedUserIdsMap;
        likedUserIdsMap = (Map<String, String>) likedUserIds;
        
        picturesComments.forEach(commentList -> likedUserIdsMap.entrySet().stream()//遍历所有点赞
                .filter(commentLiked -> commentList.getId().toString().equals(commentLiked.getKey())) //判断点赞的key和评论的id是否相等
                .findFirst()
                .ifPresent(commentLiked -> commentList.setLikedUser(commentLiked.getValue())));//相等则把点赞的用户赋值给评论对象
        
        return picturesComments;
    }

    /*
     * 查询最近更新的三条我的图片记录
     * */
    @Override
    public List<pictures> getMyPicturesIndexByUpdateTime(Long userId) {
        return picturesMapper.getMyPicturesIndexByUpdateTime(userId);
    }

    /*
     * 图片点赞
     * */
    @Override
    public boolean liked(Long picturesId, Long userId) {
        //判断是否点过赞
        Double getPictures = stringRedisTemplate.opsForZSet().score(PICTURES_LIKED + picturesId, String.valueOf(userId));
        if (getPictures != null) {//点过赞
            //删除点赞信息
            stringRedisTemplate.opsForZSet().remove(PICTURES_LIKED + picturesId, String.valueOf(userId));
            return true;
        } else { //没点过赞
            stringRedisTemplate.opsForZSet().add(PICTURES_LIKED + picturesId, String.valueOf(userId), new Date().getTime());
            return false;
        }
    }

    /*
     * 查询是否点赞
     * */
    @Override
    public boolean getStatus(Long picturesId, Long userId) {
        Double getPictures = stringRedisTemplate.opsForZSet().score(PICTURES_LIKED + picturesId.toString(), userId.toString());
        if (getPictures == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * 查询五条图片记录,根据点赞数降序排列
     * */
    @Override
    public List<pictures> getPicturesListFiveBylike() {
        //查询数据库
        QueryWrapper<pictures> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "pictures_address").
                orderByDesc("liked").
                last("limit 5");
        //返回图片列表
        return picturesMapper.selectList(queryWrapper);

    }

    /*
     * 删除图片
     * */
    @Override
    @Transactional
    public boolean removePicturesById(Long id) {
        boolean isSuccess = picturesService.removeById(id);
        //删除该图片的评论信息
        QueryWrapper<comment> queryWrapperComment = new QueryWrapper<>();
        queryWrapperComment.eq("pictures_id", id);
        commentService.remove(queryWrapperComment);
        //删除图片缓存
        String redisPicturesDetail = stringRedisTemplate.opsForValue().get(PICTURES_DETAIL + id);
        if (StrUtil.isNotBlank(redisPicturesDetail)) {
            stringRedisTemplate.delete(PICTURES_DETAIL + id);
        }
        //删除图片的点赞信息
        Set<String> redisPicturesLikedUser = stringRedisTemplate.opsForZSet().range(PICTURES_LIKED + id, 0, -1);
        if (redisPicturesLikedUser != null) {
            stringRedisTemplate.delete(PICTURES_LIKED + id);
        }
        return isSuccess;
    }

    /*
     * 统计点赞数
     * */
    @Override
    public Integer getLikedCount(Long id) {
        int liked = stringRedisTemplate.opsForZSet().size(PICTURES_LIKED + id.toString()).intValue();
        pictures pictures = new pictures();
        pictures.setId(id);
        pictures.setLiked(liked);
        picturesService.updateById(pictures);
        return liked;
    }

    /*
     * 获取点赞的前三名用户
     * */
    @Override
    public List<user> getLikedUserThree(Long id) {
        Set<String> range = stringRedisTemplate.opsForZSet().range(PICTURES_LIKED + id.toString(), 0, 2);
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
