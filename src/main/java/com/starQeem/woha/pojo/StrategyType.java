package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Date: 2023/4/22 19:38
 * @author: Qeem
 */
@Data
@TableName(value = "strategy_type")
public class StrategyType {
    @TableId(type = IdType.AUTO)
    private Long id;  //主键id
    private String name;  //文章类型名称
}
