package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.userTask;

/**
 * @Date: 2023/4/22 14:58
 * @author: Qeem
 */
public interface userTaskService extends IService<userTask> {
    userTask getMyTaskByUserId();
    userTask getGradeByUserId();
}
