package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.mapper.picturesMapper;
import com.starQeem.woha.mapper.storyMapper;
import com.starQeem.woha.mapper.strategyMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.starQeem.woha.util.constant.COMMENT_LIKED;
import static com.starQeem.woha.util.constant.STATUS_ONE;

/**
 * @Date: 2023/4/29 17:47
 * @author: Qeem
 */
@Service
public class commentServiceImpl extends ServiceImpl<commentMapper, comment> implements commentService {
    @Resource
    private commentService commentService;
    @Resource
    private picturesService picturesService;
    @Resource
    private storyService storyService;
    @Resource
    private strategyService strategyService;
    @Resource
    private commentMapper commentMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /*
     * 评论发布
     * */
    @Override
    public boolean Comment(comment comment) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        comment.setUserId(Long.valueOf(user.getId()));
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        if (comment.getPicturesId() != null) {
            QueryWrapper<pictures> storyQueryWrapper = new QueryWrapper<>();
            storyQueryWrapper.select("id","user_id").eq("id", comment.getPicturesId());
            pictures pictures = picturesService.getBaseMapper().selectOne(storyQueryWrapper);
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",pictures.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            picturesService.update(updateWrapper);
            if (Objects.equals(pictures.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
        }
        if (comment.getStoryId() != null) {
            QueryWrapper<story> storyQueryWrapper = new QueryWrapper<>();
            storyQueryWrapper.select("id","user_id").eq("id", comment.getStoryId());
            story story = storyService.getBaseMapper().selectOne(storyQueryWrapper);
            UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",story.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            storyService.update(updateWrapper);
            if (Objects.equals(story.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
        }
        if (comment.getStrategyId() != null) {
            QueryWrapper<strategy> strategyQueryWrapper = new QueryWrapper<>();
            strategyQueryWrapper.select("id","user_id").eq("id", comment.getStrategyId());
            strategy strategy = strategyService.getBaseMapper().selectOne(strategyQueryWrapper);
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",strategy.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            strategyService.update(updateWrapper);
            if (Objects.equals(strategy.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
        }
        return commentService.save(comment);
    }

    /*
     * 评论删除
     * */
    @Override
    @Transactional
    public boolean removeComment(Long id) {
        //根据id查询评论
        comment comment = commentService.getBaseMapper().selectById(id);
        //根据id查询子评论
        QueryWrapper<comment> queryWrapper = new QueryWrapper<>();
        List<comment> getCommentByParentCommentIdWithId
                = commentService.getBaseMapper().selectList(queryWrapper.eq("parent_comment_id", id));
        if (comment.getParentCommentId() != -1 || getCommentByParentCommentIdWithId == null) {
            //是子评论或者是没有子评论的父评论,直接根据id删除
            boolean success = commentService.removeById(id);
            if (success) {
                if (comment.getPicturesId() != null) {
                    UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getPicturesId()).setSql("comment_count = comment_count - 1");  //评论数+1
                    picturesService.update(updateWrapper);
                }
                if (comment.getStoryId() != null) {
                    UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStoryId()).setSql("comment_count = comment_count - 1"); //评论数+1
                    picturesService.update(updateWrapper);
                }
                if (comment.getStrategyId() != null) {
                    UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStrategyId()).setSql("comment_count = comment_count - 1"); //评论数+1
                    strategyService.update(updateWrapper);
                }
            }
            return success;
        } else {
            //是父评论且有子评论,父评论和子评论一起删除
            queryWrapper.eq("parent_comment_id", id).or(wrapper -> wrapper.eq("id", id));
            int i = commentService.getBaseMapper().delete(queryWrapper);
            if (i > 0) {
                if (comment.getPicturesId() != null) {
                    UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getPicturesId()).setSql("comment_count = comment_count - " + i);  //评论数-i
                    picturesService.update(updateWrapper);
                }
                if (comment.getStoryId() != null) {
                    UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStoryId()).setSql("comment_count = comment_count - " + i); //评论数-i
                    picturesService.update(updateWrapper);
                }
                if (comment.getStrategyId() != null) {
                    UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStrategyId()).setSql("comment_count = comment_count - " + i); //评论数-i
                    strategyService.update(updateWrapper);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * 评论区点赞
     * */
    @Override
    public void liked(Long commentId, Long userId) {
        //获取点赞的用户ids
        Object likedUserIds = stringRedisTemplate.opsForHash().get(COMMENT_LIKED, commentId.toString());
        if (likedUserIds == null){
            //没有用户点过赞
            stringRedisTemplate.opsForHash().put(COMMENT_LIKED,commentId.toString(),userId.toString());
        }else {
            //有用户点过赞
            boolean isLiked = likedUserIds.toString().contains(userId.toString());
            //判断是否点过赞
            if (isLiked){//点过赞,取消赞
                String[] idArray = likedUserIds.toString().split(",");
                // 将字符串数组转换成 Stream<String> 对象
                Stream<String> idStream = Arrays.stream(idArray);
                // 过滤掉要删除的 ID
                Stream<String> filteredIdStream = idStream.filter(id -> !id.equals(String.valueOf(userId)));
                // 将过滤后的字符串数组转换成以逗号分隔的字符串
                String newIds = filteredIdStream.collect(Collectors.joining(","));
                stringRedisTemplate.opsForHash().put(COMMENT_LIKED,commentId.toString(),newIds);
            }else {//没点过赞
                if (likedUserIds.equals("")) {
                    stringRedisTemplate.opsForHash().put(COMMENT_LIKED,commentId.toString(),userId.toString());
                } else {
                    stringRedisTemplate.opsForHash().put(COMMENT_LIKED,commentId.toString(),likedUserIds + "," + userId);
                }
            }
        }
    }
    /*
    * 回复我的评论
    * */
    @Override
    public PageInfo<comment> info(Integer pageNum, int pageSize) {
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        List<comment> commentPicturesInfo = commentMapper.commentPicturesInfo(Long.valueOf(user.getId()));
        List<comment> commentStrategyInfo = commentMapper.commentStrategyInfo(Long.valueOf(user.getId()));
        List<comment> commentStoryInfo = commentMapper.commentStoryInfo(Long.valueOf(user.getId()));
        List<comment> commentList = new ArrayList<>();
        commentList.addAll(commentPicturesInfo);
        commentList.addAll(commentStrategyInfo);
        commentList.addAll(commentStoryInfo);
        // 按照 update_time 属性降序排列
        commentList.sort(Comparator.comparing(comment::getUpdateTime).reversed());

        int total = commentList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<comment> pagedCommentList = commentList.subList(fromIndex, toIndex);

        PageInfo<comment> pageInfo = new PageInfo<>();  
        pageInfo.setList(pagedCommentList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);
        // 判断是否有上一页和下一页
        boolean hasPreviousPage = pageNum > 1;
        boolean hasNextPage = pageNum < totalPages;

        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setHasNextPage(hasNextPage);

        return pageInfo;
    }
    /*
     * 我的所有文章的评论
     * */
    @Override
    public PageInfo<comment> comment(Integer pageNum, int pageSize) {
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        List<comment> picturesComment = commentMapper.picturesComment(Long.valueOf(user.getId()));//图片文章评论
        List<comment> strategyComment = commentMapper.strategyComment(Long.valueOf(user.getId()));//攻略文章评论
        List<comment> storyComment = commentMapper.storyComment(Long.valueOf(user.getId()));//广场文章评论
        //将3个查询结果的集合合并为1个
        List<comment> commentList = new ArrayList<>();
        commentList.addAll(picturesComment);
        commentList.addAll(strategyComment);
        commentList.addAll(storyComment);
        // 对合并出来的集合按照 update_time 属性降序排列
        commentList.sort(Comparator.comparing(comment::getUpdateTime).reversed());

        int total = commentList.size();  //数据总条数
        int fromIndex = (pageNum - 1) * pageSize;  //截止当前页码数的所有数据条数
        int toIndex = Math.min(fromIndex + pageSize, total); //分页查询中的结束索引位置

        List<comment> pagedCommentList = commentList.subList(fromIndex, toIndex);  //调用subList进行分页

        PageInfo<comment> pageInfo = new PageInfo<>();
        pageInfo.setList(pagedCommentList);  //存入合并出来的集合
        pageInfo.setTotal(total);  //存入数据总条数
        pageInfo.setPageNum(pageNum);  //存入当前页码数
        pageInfo.setPageSize(pageSize); //存入一页有多少条数据

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 判断是否有上一页和下一页
        boolean hasPreviousPage = pageNum > 1;
        boolean hasNextPage = pageNum < totalPages;

        //将是否有上一页下一页的信息存入pageInfo对象中
        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setHasNextPage(hasNextPage);

        return pageInfo;
    }
}
