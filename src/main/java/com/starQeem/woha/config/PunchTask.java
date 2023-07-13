package com.starQeem.woha.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.starQeem.woha.pojo.UserTask;
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
        //设置为未完成
        userTaskService.update(Wrappers.<UserTask>lambdaUpdate()
                .set(UserTask::getDailytaskStrategy,STATUS_ZERO)
                .set(UserTask::getDailytaskStory,STATUS_ZERO)
                .set(UserTask::getDailytaskLogin,STATUS_ZERO));
    }
    @Scheduled(cron = "0 0 0 ? * MON")  //每周一0点刷新每周任务状态
    public void resetPunchAndTaskStatusWeek() {
        userTaskService.update(Wrappers.<UserTask>lambdaUpdate().set(UserTask::getWeeklytaskPictures,STATUS_ZERO));
    }
}