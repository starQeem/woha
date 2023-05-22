package com.starQeem.woha.config;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.starQeem.woha.pojo.userTask;
import com.starQeem.woha.service.userTaskService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

import static com.starQeem.woha.util.constant.STATUS_ZERO;

/**
 * @Date: 2023/4/24 16:08
 * @author: Qeem
 * 定时任务
 */
@Configuration
@EnableScheduling
public class PunchTask {
    @Resource
    private userTaskService userTaskService;
    @Scheduled(cron = "0 0 0 * * ?")  //每日0点每日刷新任务状态
    public void resetPunchAndTaskStatusDay() {
        UpdateWrapper<userTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("dailytask_strategy", STATUS_ZERO).set("dailytask_story",STATUS_ZERO).set("dailytask_login",STATUS_ZERO);
        userTaskService.update(updateWrapper);
    }
    @Scheduled(cron = "0 0 0 ? * MON")  //每周一0点刷新每周任务状态
    public void resetPunchAndTaskStatusWeek() {
        UpdateWrapper<userTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("weeklytask_pictures", STATUS_ZERO);
        userTaskService.update(updateWrapper);
    }
}