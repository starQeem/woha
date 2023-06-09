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
import com.starQeem.woha.mapper.picturesMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
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
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("create_time desc");
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        List<pictures> picturesList = picturesMapper.getUserWithPictures(Long.valueOf(user.getId()));
        PageInfo<pictures> pageInfo = new PageInfo<>(picturesList, pageSize);
        return pageInfo;
    }

    /*
     * 查询图片列表
     * */
    @Override
    public PageInfo<pictures> getPicturesListPageInfo(Integer pageNum, int pageSize, String title) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        if (title == null){
            title = "";
        }else {
            pageSize = SEARCH_SIZE;
        }
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("liked desc");
        PageHelper.orderBy("create_time desc");
        //查询redis中有没有列表
        String getPicturesListJson = stringRedisTemplate.opsForValue().get(PICTURES_LIST + pageNum);
        if (StrUtil.isNotBlank(getPicturesListJson) && StrUtil.isBlank(title)) {
            //有列表,直接返回redis中查到的数据
            JSONObject jsonObj = JSONUtil.parseObj(getPicturesListJson);
            // 将JSONObject对象转换为PageInfo<Pictures>对象
            PageInfo<pictures> pageInfo = (PageInfo<pictures>) jsonObj.toBean(PageInfo.class);
            return pageInfo;
        } else {
            //没有列表,查询数据库
            List<pictures> picturesList = picturesMapper.getPicturesListPageInfo(title);
            //将数据库中的列表信息存入redis中
            PageInfo<pictures> pageInfo = new PageInfo<>(picturesList, pageSize);
            if (StrUtil.isBlank(title)) {
                JSONObject jsonObj = JSONUtil.parseObj(pageInfo);
                // 将JSONObject对象转换为PageInfo<Story>对象
                PageInfo<pictures> picturesListPageInfo = (PageInfo<pictures>) jsonObj.toBean(PageInfo.class);
                String redisPicturesList = JSONUtil.toJsonStr(picturesListPageInfo);
                stringRedisTemplate.opsForValue().set(PICTURES_LIST + pageNum, redisPicturesList, TIME_SMALL, TimeUnit.SECONDS);
            }
            //将List集合丢到分页对象里
            return pageInfo;
        }
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
            if (userTask.getExperience() >= GRADE_SIX) {  //判断用户经验值达到的等级
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
        for (comment commentList : picturesComments){  //遍历所有评论
            for (Map.Entry<String, String> commentLiked : likedUserIdsMap.entrySet()){  //遍历所有点赞
                 if (commentList.getId().toString().equals(commentLiked.getKey())){  //判断点赞的key和评论的id是否相等
                     commentList.setLikedUser(commentLiked.getValue());  //相等则把点赞的用户赋值给评论对象
                 }
            }
        }
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
        //查询redis中是否存在图片列表
        String getPicturesFiveList = stringRedisTemplate.opsForValue().get(PICTURES_FIVE_LIST);
        if (StrUtil.isNotBlank(getPicturesFiveList)) {
            //存在,直接返回
            List<pictures> picturesList = JSONUtil.toList(JSONUtil.parseArray(getPicturesFiveList), pictures.class);
            return picturesList;
        } else {
            //不存在,查询数据库
            QueryWrapper<pictures> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "pictures_address").
                    orderByDesc("liked").
                    last("limit 5");
            List<pictures> picturesList = picturesMapper.selectList(queryWrapper);
            //将查询出的数据存入redis中
            stringRedisTemplate.opsForValue().set(PICTURES_FIVE_LIST, JSONUtil.toJsonStr(picturesList), TIME_SMALL, TimeUnit.SECONDS);
            //返回图片列表
            return picturesList;
        }
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
        Set<String> redisPicturesLikedUser = stringRedisTemplate.opsForZSet().range(PICTURES_LIKED + String.valueOf(id), 0, -1);
        if (redisPicturesLikedUser != null) {
            stringRedisTemplate.delete(PICTURES_LIKED + String.valueOf(id));
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
            List<user> ThreeUserLikedList = userService.getBaseMapper().selectList(queryWrapper);
            return ThreeUserLikedList;
        } else {
            return null;
        }
    }
}
