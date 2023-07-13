package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Date: 2023/4/18 10:45
 * @author: Qeem
 */
@TableName(value = "Pictures")
@Data
public class Pictures {
    @TableId(type = IdType.AUTO)
    private Long id;   //主键id
    private String picturesAddress;  //图片地址
    private String title;   //标题
    private String content;  //图片介绍
    private Integer views;   //浏览次数
    private Integer commentCount;  //评论数
    private Integer liked; //点赞数
    private Date updateTime;  //更新时间
    private Date createTime;  //创建时间
    private Long userId;  //用户id
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private UserTask userTask;
}
