package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Date: 2023/4/17 0:00
 * @author: Qeem
 * 用户
 */
@Data
@TableName(value ="user")
public class user {
    @TableId(type = IdType.AUTO)
    private Integer id;   //主键id
    private String username;   //用户名
    private String password;    //密码
    private String nickName;   //用户昵称
    private String sex;   //用户性别
    private String hobby;   //爱好
    private String email;  //邮箱
    private String signature;  //签名
    private String avatar;  //头像
    @TableField(exist = false)
    private userTask userTask;
    @TableField(exist = false)
    private follow follow;
}
