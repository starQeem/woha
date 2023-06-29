package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.config.email;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
import com.starQeem.woha.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.starQeem.woha.util.constant.*;

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
    @Resource
    private sendEmailService sendEmailService;

    /*
     * 评论发布
     * */
    @Override
    public boolean Comment(comment comment) throws MessagingException {
        if (comment.getPicturesId() == null && comment.getStrategyId() == null && comment.getStoryId() == null) {
            return false;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        comment.setUserId(Long.valueOf(user.getId()));
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());

        String title = "";
        Integer type = STATUS_ZERO;

        if (comment.getPicturesId() != null) {
            pictures pictures = getPicturesById(comment.getPicturesId());
            incrementCommentCount(pictures);
            if (pictures.getUserId() == Long.valueOf(user.getId())) {
                comment.setIsAdmin(STATUS_ONE);
            }
            title = pictures.getTitle();
            type = TYPE_ONE;
        }

        if (comment.getStoryId() != null) {
            story story = getStoryById(comment.getStoryId());
            incrementCommentCount(story);
            if (story.getUserId() == Long.valueOf(user.getId())) {
                comment.setIsAdmin(STATUS_ONE);
            }
            title = story.getTitle();
            type = TYPE_TWO;
        }

        if (comment.getStrategyId() != null) {
            strategy strategy = getStrategyById(comment.getStrategyId());
            incrementCommentCount(strategy);
            if (strategy.getUserId() == Long.valueOf(user.getId())) {
                comment.setIsAdmin(STATUS_ONE);
            }
            title = strategy.getTitle();
            type = TYPE_THREE;
        }
        //回复评论时发邮箱提醒
        if (comment.getCommentUserId() != null && !comment.getCommentUserId().equals(comment.getUserId())) {
            sendEmailService.sendEmail(comment, title, type);
        }

        return commentService.save(comment);
    }

    /**
     * 通过id获取图片
     *
     * @param picturesId 身份证照片
     * @return {@link pictures}
     */
    private pictures getPicturesById(Long picturesId) {
        QueryWrapper<pictures> wrapper = new QueryWrapper<>();
        wrapper.select("id", "user_id", "title").eq("id", picturesId);
        return picturesService.getBaseMapper().selectOne(wrapper);
    }

    /**
     * 通过id获取故事
     *
     * @param storyId 故事id
     * @return {@link story}
     */
    private story getStoryById(Long storyId) {
        QueryWrapper<story> wrapper = new QueryWrapper<>();
        wrapper.select("id", "user_id", "title").eq("id", storyId);
        return storyService.getBaseMapper().selectOne(wrapper);
    }

    /**
     * 通过id获取策略
     *
     * @param strategyId 战略id
     * @return {@link strategy}
     */
    private strategy getStrategyById(Long strategyId) {
        QueryWrapper<strategy> wrapper = new QueryWrapper<>();
        wrapper.select("id", "user_id", "title").eq("id", strategyId);
        return strategyService.getBaseMapper().selectOne(wrapper);
    }

    /**
     * 增加图片评论数
     *
     * @param pictures 图片
     */
    private void incrementCommentCount(pictures pictures) {
        UpdateWrapper<pictures> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", pictures.getId()).setSql("comment_count = comment_count + 1");
        picturesService.update(wrapper);
    }

    /**
     * 增加问答评论数
     *
     * @param story 故事
     */
    private void incrementCommentCount(story story) {
        UpdateWrapper<story> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", story.getId()).setSql("comment_count = comment_count + 1");
        storyService.update(wrapper);
    }

    /**
     * 增加文章评论数
     *
     * @param strategy 策略
     */
    private void incrementCommentCount(strategy strategy) {
        UpdateWrapper<strategy> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", strategy.getId()).setSql("comment_count = comment_count + 1");
        strategyService.update(wrapper);
    }

    /*
     * 评论删除
     * */
    @Override
    @Transactional
    public boolean removeComment(Long id) {
        comment comment = commentService.getById(id);

        if (comment == null) {
            return false;
        }

        QueryWrapper<comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_comment_id", id);
        List<comment> childComments = commentService.list(queryWrapper);

        if (comment.getParentCommentId() != -1 || childComments.isEmpty()) {
            // 是子评论或者是没有子评论的父评论,直接根据id删除
            boolean success = commentService.removeById(id);
            if (success) {
                decrementCommentCount(comment);
            }
            return success;
        } else {
            // 是父评论且有子评论,父评论和子评论一起删除
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_comment_id", id).or().eq("id", id);
            int deletedCount = commentService.count(queryWrapper);

            boolean success = commentService.remove(queryWrapper);
            if (success) {
                decrementCommentCount(comment, deletedCount);
            }
            return success;
        }
    }

    /**
     * 没有子评论的减少评论数
     *
     * @param comment 评论
     */
    private void decrementCommentCount(comment comment) {
        if (comment.getPicturesId() != null) {
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getPicturesId()).setSql("comment_count = comment_count - 1");
            picturesService.update(updateWrapper);
        }
        if (comment.getStoryId() != null) {
            UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getStoryId()).setSql("comment_count = comment_count - 1");
            storyService.update(updateWrapper);
        }
        if (comment.getStrategyId() != null) {
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getStrategyId()).setSql("comment_count = comment_count - 1");
            strategyService.update(updateWrapper);
        }
    }

    /**
     * 有子评论的减少评论数
     *
     * @param comment 评论
     * @param count   数
     */
    private void decrementCommentCount(comment comment, int count) {
        if (comment.getPicturesId() != null) {
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getPicturesId()).setSql("comment_count = comment_count - " + count);
            picturesService.update(updateWrapper);
        }
        if (comment.getStoryId() != null) {
            UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getStoryId()).setSql("comment_count = comment_count - " + count);
            storyService.update(updateWrapper);
        }
        if (comment.getStrategyId() != null) {
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getStrategyId()).setSql("comment_count = comment_count - " + count);
            strategyService.update(updateWrapper);
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
                String newIds = StringUtils.delULikeId(likedUserIds.toString(), userId);
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
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        List<comment> commentPicturesInfo = commentMapper.commentPicturesInfo(Long.valueOf(user.getId()));
        List<comment> commentStrategyInfo = commentMapper.commentStrategyInfo(Long.valueOf(user.getId()));
        List<comment> commentStoryInfo = commentMapper.commentStoryInfo(Long.valueOf(user.getId()));
        return mergeList(commentPicturesInfo, commentStrategyInfo, commentStoryInfo, pageNum, pageSize);
    }
    /*
     * 我的所有文章的评论
     * */
    @Override
    public PageInfo<comment> comment(Integer pageNum, int pageSize) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        List<comment> picturesComment = commentMapper.picturesComment(Long.valueOf(user.getId()));//图片文章评论
        List<comment> strategyComment = commentMapper.strategyComment(Long.valueOf(user.getId()));//攻略文章评论
        List<comment> storyComment = commentMapper.storyComment(Long.valueOf(user.getId()));//广场文章评论
        return mergeList(picturesComment,strategyComment,storyComment,pageNum,pageSize);

    }

    /**
     * 合并集合列表并分页
     *
     * @param commentList1 评论list1
     * @param commentList2 注释用于
     * @param commentList3 评论list3
     * @param pageNum      页面num
     * @param pageSize     页面大小
     * @return {@link PageInfo}<{@link comment}>
     */
    private PageInfo<comment> mergeList(List<comment> commentList1,List<comment> commentList2,List<comment> commentList3,Integer pageNum,int pageSize){
        List<comment> commentList = new ArrayList<>();
        commentList.addAll(commentList1);
        commentList.addAll(commentList2);
        commentList.addAll(commentList3);
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

        int[] nums = new int[totalPages];
        for (int i = 0; i < totalPages; i++) {
            nums[i] = i + 1;
        }
        pageInfo.setNavigatepageNums(nums); //存入页码数组
        // 判断是否有上一页和下一页
        boolean hasPreviousPage = pageNum > 1;
        boolean hasNextPage = pageNum < totalPages;

        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setHasNextPage(hasNextPage);
        return pageInfo;
    }
}
