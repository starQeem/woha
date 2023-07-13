package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Date: 2023/4/28 17:14
 * @author: Qeem
 */
@Data
@TableName(value = "Follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;  //主键id
    private Long userId;  //用户id
    private Long followUserId;  //关注的用户的id
}
