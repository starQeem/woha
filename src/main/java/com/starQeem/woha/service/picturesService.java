package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.starQeem.woha.pojo.comment;
import com.starQeem.woha.pojo.pictures;
import com.starQeem.woha.pojo.user;

import java.util.List;

/**
 * @Date: 2023/4/18 10:51
 * @author: Qeem
 */
public interface picturesService extends IService<pictures> {
    boolean savePictures(pictures pictures);
    PageInfo<pictures> queryPictures(Integer pageNum,int pageSize);
    boolean updatePictures(pictures pictures);
    pictures queryPicturesDetail(Long id);
    List<pictures> getMyPicturesIndexByUpdateTime(Long userId);
    PageInfo<pictures> getPicturesListPageInfo(Integer pageNum, int pageSize, String title);
    pictures getPicturesDetailById(Long id);
    List<pictures> getUserPicturesWithUpdateTime(Long id);
    List<comment> getComments(Long id);
    boolean liked(Long picturesId, Long userId);
    boolean getStatus(Long picturesId, Long userId);
    List<pictures> getPicturesListFiveBylike();
    boolean removePicturesById(Long id);
    Integer getLikedCount(Long id);
    List<user> getLikedUserThree(Long id);
}
