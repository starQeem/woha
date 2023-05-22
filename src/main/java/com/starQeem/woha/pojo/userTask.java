package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Date: 2023/4/22 14:44
 * @author: Qeem
 */
@Data
@TableName(value = "user_task")
public class userTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer experience;  //用户经验
    private Integer grade;  //用户等级
    private Long userId;  //用户id
    private Integer dailytaskStrategy;  //每日任务,观看一篇百科
    private Integer dailytaskStory;  //每日任务,观看一篇故事
    private Integer dailytaskLogin;  //每日任务,登录
    private Integer weeklytaskPictures;  //每周任务,发布一张图片
    @TableField(exist = false)
    private user user;
}
