package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.story;
import com.starQeem.woha.pojo.user;

import java.util.List;

/**
 * @Date: 2023/4/17 15:42
 * @author: Qeem
 */
public interface storyService extends IService<story>{
    boolean saveStory(story story);
    PageInfo<story> queryMyStory(Integer pageNum,int PAGE_SIZE,Long id);
    story queryStoryDetail(Long id);
    boolean updateStory(story story);
    List<story> getMyPicturesIndexByUpdateTime(Long userId);
    PageInfo<story> getStoryListPageInfo(Integer pageNum, int pageSize, String title);
    story getStoryById(Long id);
    List<comment> getComments(Long id);
    boolean liked(Long storyId, Long userId);
    boolean getStatus(Long storyId);
    List<story> getStoryListFive();
    boolean removeStoryById(Long id);
    Integer getLikedCount(Long id);
    List<user> getLikedUserThree(Long id);
}
