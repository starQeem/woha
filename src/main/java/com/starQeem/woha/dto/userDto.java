package com.starQeem.woha.dto;

import lombok.Data;

/**
 * @Date: 2023/5/18 13:52
 * @author: Qeem
 */
@Data
public class userDto {
    private Integer id;   //主键id
    private String username;   //用户名
    private String perms; //用户权限
}
