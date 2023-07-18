package com.starQeem.woha.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.mapper.commentMapper;
import com.starQeem.woha.pojo.*;
import com.starQeem.woha.service.*;
import com.starQeem.woha.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.*;

import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/4/29 17:47
 * @author: Qeem
 */
@Service
public class commentServiceImpl extends ServiceImpl<commentMapper, Comment> implements commentService {
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
    public boolean Comment(Comment comment) throws MessagingException {
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
            Pictures pictures = getPicturesById(comment.getPicturesId());
            incrementCommentCount(pictures);
            if (pictures.getUserId() == Long.valueOf(user.getId())) {
                comment.setIsAdmin(STATUS_ONE);
            }
            title = pictures.getTitle();
            type = TYPE_ONE;
        }

        if (comment.getStoryId() != null) {
            Story story = getStoryById(comment.getStoryId());
            incrementCommentCount(story);
            if (story.getUserId() == Long.valueOf(user.getId())) {
                comment.setIsAdmin(STATUS_ONE);
            }
            title = story.getTitle();
            type = TYPE_TWO;
        }

        if (comment.getStrategyId() != null) {
            Strategy strategy = getStrategyById(comment.getStrategyId());
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

        return save(comment);
    }

    /**
     * 通过id获取图片
     *
     * @param picturesId 身份证照片
     * @return {@link Pictures}
     */
    private Pictures getPicturesById(Long picturesId) {
        return picturesService.getBaseMapper().selectOne(Wrappers.<Pictures>lambdaQuery()
                .select(Pictures::getId, Pictures::getUserId, Pictures::getTitle)
                .eq(Pictures::getId,picturesId));
    }

    /**
     * 通过id获取故事
     *
     * @param storyId 故事id
     * @return {@link Story}
     */
    private Story getStoryById(Long storyId) {
        return storyService.getBaseMapper().selectOne(Wrappers.<Story>lambdaQuery()
                .select(Story::getId, Story::getUserId, Story::getTitle)
                .eq(Story::getId,storyId));
    }

    /**
     * 通过id获取策略
     *
     * @param strategyId 战略id
     * @return {@link Strategy}
     */
    private Strategy getStrategyById(Long strategyId) {
        return strategyService.getBaseMapper().selectOne(Wrappers.<Strategy>lambdaQuery()
                .select(Strategy::getId, Strategy::getUserId, Strategy::getTitle)
                .eq(Strategy::getId,strategyId));
    }

    /**
     * 增加图片评论数
     *
     * @param pictures 图片
     */
    private void incrementCommentCount(Pictures pictures) {
        picturesService.update(Wrappers.<Pictures>lambdaUpdate()
                .eq(Pictures::getId,pictures.getId())
                .setSql("comment_count = comment_count + 1"));
    }

    /**
     * 增加问答评论数
     *
     * @param story 故事
     */
    private void incrementCommentCount(Story story) {
        storyService.update(Wrappers.<Story>lambdaUpdate()
                .eq(Story::getId,story.getId())
                .setSql("comment_count = comment_count + 1"));
    }

    /**
     * 增加文章评论数
     *
     * @param strategy 策略
     */
    private void incrementCommentCount(Strategy strategy) {
        strategyService.update(Wrappers.<Strategy>lambdaUpdate()
                .eq(Strategy::getId,strategy.getId())
                .setSql("comment_count = comment_count + 1"));
    }

    /*
     * 评论删除
     * */
    @Override
    @Transactional
    public boolean removeComment(Long id) {
        if (id == null){
            return false;
        }
        Comment commentById = commentService.getById(id);

        if (commentById == null) {
            return false;
        }

        List<Comment> childComments = commentService.list(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getParentCommentId,id));

        if (commentById.getParentCommentId() != -1 || childComments.isEmpty()) {
            // 是子评论或者是没有子评论的父评论,直接根据id删除
            boolean success = commentService.removeById(id);
            if (success) {
                decrementCommentCount(commentById);
            }
            return success;
        } else {
            // 是父评论且有子评论,父评论和子评论一起删除
            int deletedCount = commentService.count(Wrappers.<Comment>lambdaQuery()
                    .eq(Comment::getParentCommentId,id)
                    .or()
                    .eq(Comment::getId,id));

            boolean success = commentService.remove(Wrappers.<Comment>lambdaQuery()
                    .eq(Comment::getParentCommentId,id)
                    .or()
                    .eq(Comment::getId,id));
            if (success) {
                decrementCommentCount(commentById, deletedCount);
            }
            return success;
        }
    }

    /**
     * 没有子评论的减少评论数
     *
     * @param comment 评论
     */
    private void decrementCommentCount(Comment comment) {
        if (comment.getPicturesId() == null && comment.getStoryId() == null && comment.getStrategyId() == null){
            return;
        }
        if (comment.getPicturesId() != null) {
            picturesService.update(Wrappers.<Pictures>update().lambda()
                    .eq(Pictures::getId,comment.getPicturesId())
                    .setSql("comment_count = comment_count - 1"));
        }
        if (comment.getStoryId() != null) {
            storyService.update(Wrappers.<Story>update().lambda()
                    .eq(Story::getId,comment.getStoryId())
                    .setSql("comment_count = comment_count - 1"));
        }
        if (comment.getStrategyId() != null) {
            strategyService.update(Wrappers.<Strategy>update().lambda()
                    .eq(Strategy::getId,comment.getStrategyId())
                    .setSql("comment_count = comment_count - 1"));
        }
    }

    /**
     * 有子评论的减少评论数
     *
     * @param comment 评论
     * @param count   数
     */
    private void decrementCommentCount(Comment comment, int count) {
        if (comment.getPicturesId() == null && comment.getStoryId() == null && comment.getStrategyId() == null){
            return;
        }
        if (comment.getPicturesId() != null) {
            picturesService.update(Wrappers.<Pictures>update().lambda()
                    .eq(Pictures::getId,comment.getPicturesId())
                    .setSql("comment_count = comment_count - " + count));
            return;
        }
        if (comment.getStoryId() != null) {
            storyService.update(Wrappers.<Story>update().lambda()
                    .eq(Story::getId,comment.getStoryId())
                    .setSql("comment_count = comment_count - " + count));
            return;
        }
        if (comment.getStrategyId() != null) {
            strategyService.update(Wrappers.<Strategy>update().lambda()
                    .eq(Strategy::getId,comment.getStrategyId())
                    .setSql("comment_count = comment_count - " + count));
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
    public PageInfo<Comment> info(Integer pageNum, int pageSize) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        List<Comment> commentPicturesInfo = commentMapper.commentPicturesInfo(Long.valueOf(user.getId()));
        List<Comment> commentStrategyInfo = commentMapper.commentStrategyInfo(Long.valueOf(user.getId()));
        List<Comment> commentStoryInfo = commentMapper.commentStoryInfo(Long.valueOf(user.getId()));
        return mergeList(commentPicturesInfo, commentStrategyInfo, commentStoryInfo, pageNum, pageSize);
    }
    /*
     * 我的所有文章的评论
     * */
    @Override
    public PageInfo<Comment> comment(Integer pageNum, int pageSize) {
        if (pageNum == null){
            pageNum = PAGE_NUM;
        }
        //获取用户信息
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();

        List<Comment> picturesComment = commentMapper.picturesComment(Long.valueOf(user.getId()));//图片评论
        List<Comment> strategyComment = commentMapper.strategyComment(Long.valueOf(user.getId()));//文章评论
        List<Comment> storyComment = commentMapper.storyComment(Long.valueOf(user.getId()));//问答评论
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
     * @return {@link PageInfo}<{@link Comment}>
     */
    private PageInfo<Comment> mergeList(List<Comment> commentList1, List<Comment> commentList2, List<Comment> commentList3, Integer pageNum, int pageSize){
        List<Comment> commentList = new ArrayList<>();
        commentList.addAll(commentList1);
        commentList.addAll(commentList2);
        commentList.addAll(commentList3);
        // 按照 update_time 属性降序排列
        commentList.sort(Comparator.comparing(Comment::getUpdateTime).reversed());

        int total = commentList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Comment> pagedCommentList = commentList.subList(fromIndex, toIndex);

        PageInfo<Comment> pageInfo = new PageInfo<>();
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
