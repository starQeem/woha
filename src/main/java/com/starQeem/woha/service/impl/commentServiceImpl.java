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
        if (comment.getPicturesId() ==null && comment.getStrategyId() == null && comment.getStoryId() == null){
            return false;
        }
        Subject subject = SecurityUtils.getSubject();
        userDto user = (userDto) subject.getPrincipal();
        comment.setUserId(Long.valueOf(user.getId()));
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        String title = "";
        Integer type = STATUS_ZERO;
        if (comment.getPicturesId() != null) {
            QueryWrapper<pictures> storyQueryWrapper = new QueryWrapper<>();
            storyQueryWrapper.select("id","user_id","title").eq("id", comment.getPicturesId());
            pictures pictures = picturesService.getBaseMapper().selectOne(storyQueryWrapper);
            UpdateWrapper<pictures> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",pictures.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            picturesService.update(updateWrapper);
            if (Objects.equals(pictures.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
            title = pictures.getTitle();
            type = TYPE_ONE;
        }
        if (comment.getStoryId() != null) {
            QueryWrapper<story> storyQueryWrapper = new QueryWrapper<>();
            storyQueryWrapper.select("id","user_id","title").eq("id", comment.getStoryId());
            story story = storyService.getBaseMapper().selectOne(storyQueryWrapper);
            UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",story.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            storyService.update(updateWrapper);
            if (Objects.equals(story.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
            title = story.getTitle();
            type = TYPE_TWO;
        }
        if (comment.getStrategyId() != null) {
            QueryWrapper<strategy> strategyQueryWrapper = new QueryWrapper<>();
            strategyQueryWrapper.select("id","user_id","title").eq("id", comment.getStrategyId());
            strategy strategy = strategyService.getBaseMapper().selectOne(strategyQueryWrapper);
            UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",strategy.getId()).setSql("comment_count = comment_count + 1"); //评论数+1
            strategyService.update(updateWrapper);
            if (Objects.equals(strategy.getUserId(), Long.valueOf(user.getId()))) {//判断是否为自己发的帖子
                //是,is_admin设置为1(表示此评论为楼主评论)
                comment.setIsAdmin(STATUS_ONE);
            }
            title = strategy.getTitle();
            type = TYPE_THREE;
        }
        //回复评论时发邮箱提醒
        if (comment.getCommentUserId()!=null && !Objects.equals(comment.getCommentUserId(), comment.getUserId())){
            sendEmailService.sendEmail(comment,title,type);
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
                    updateWrapper.eq("id",comment.getPicturesId()).setSql("comment_count = comment_count - 1");  //评论数-1
                    picturesService.update(updateWrapper);
                }
                if (comment.getStoryId() != null) {
                    UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStoryId()).setSql("comment_count = comment_count - 1"); //评论数-1
                    storyService.update(updateWrapper);
                }
                if (comment.getStrategyId() != null) {
                    UpdateWrapper<strategy> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStrategyId()).setSql("comment_count = comment_count - 1"); //评论数-1
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
                    UpdateWrapper<story> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id",comment.getStoryId()).setSql("comment_count = comment_count - " + i); //评论数-i
                    storyService.update(updateWrapper);
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
