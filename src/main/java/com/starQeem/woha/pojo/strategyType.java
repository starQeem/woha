package com.starQeem.woha.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Date: 2023/4/22 19:38
 * @author: Qeem
 */
@Data
@TableName(value = "strategy_type")
public class strategyType {
    private Long id;  //主键id
    private String name;  //攻略类型名称
}
