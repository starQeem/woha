package com.starQeem.woha.util;

import com.starQeem.woha.mapper.userTaskMapper;
import com.starQeem.woha.pojo.userTask;
import com.starQeem.woha.service.userTaskService;

import javax.annotation.Resource;

import static com.starQeem.woha.util.constant.GRADE_SIX;

/**
 * @Date: 2023/6/29 10:52
 * @author: Qeem
 */
public class updateGradeUtils {
    public static userTask updateGrade(userTask userTask){
        if (userTask.getGrade() == 6){
            return userTask;
        }
        if (userTask.getExperience() >= GRADE_SIX) {  //判断用户经验值达到的等级
            userTask.setGrade(6);
        } else {
            switch (userTask.getExperience() / 100) {
                case 9:
                case 8:
                case 7:
                    userTask.setGrade(5);
                    break;
                case 6:
                case 5:
                    userTask.setGrade(4);
                    break;
                case 4:
                    userTask.setGrade(3);
                    break;
                case 3:
                    userTask.setGrade(2);
                    break;
                default:
                    userTask.setGrade(1);
            }
        }
        return userTask;
    }
}
