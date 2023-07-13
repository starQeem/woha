package com.starQeem.woha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starQeem.woha.pojo.UserTask;

/**
 * @Date: 2023/4/22 14:58
 * @author: Qeem
 */
public interface userTaskService extends IService<UserTask> {
    /**
     * 查询我的任务
     *
     * @return {@link UserTask}
     */
    UserTask getMyTaskByUserId();

    /**
     * 查询我的等级
     *
     * @return {@link UserTask}
     */
    UserTask getGradeByUserId();
}
