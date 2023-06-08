package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Date: 2023/4/29 17:42
 * @author: Qeem
 */
@Data
@TableName(value = "comment")
public class comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;  //评论内容
    private String commentNickName;  //评论的人的昵称
    @TableField(exist = false)
    private String likedUser;  //点赞的用户
    private Integer isAdmin;  //是否为楼主
    @TableField(exist = false)
    private Integer liked;  //点赞数
    private Long userId;  //用户id
    private Long picturesId;  //图片id
    private Long storyId;   //故事id
    private Long parentCommentId;  //子评论id
    private Long commentUserId;  //回复的人的id
    private Long strategyId;  //攻略id
    private Date createTime;  //创建时间
    private Date updateTime;   //更新时间
    @TableField(exist = false)
    private user user;
    @TableField(exist = false)
    private pictures pictures;
    @TableField(exist = false)
    private story story;
    @TableField(exist = false)
    private strategy strategy;
    @TableField(exist = false)
    private userTask userTask;
}
