package com.starQeem.woha.util;

import com.sun.org.apache.bcel.internal.generic.PUSH;

/**
 * @Date: 2023/5/6 9:14
 * @author: Qeem
 */
public class constant {
    public final static int PAGE_SIZE = 8;  //数据条数
    public final static int PICTURES_PAGE_SIZE = 9;  //图片数据条数
    public final static int COMMENT_PAGE_SIZE = 15;  //查看回复的数据条数
    public final static int PAGE_NUM = 1;  //默认页码
    public final static int TASK_DAY_EXPERIENCE = 10;   //每日任务经验值
    public final static int TASK_WEEK_EXPERIENCE = 50;   //每周任务经验值
    public final static int GRADE_SIX = 1000;  //用户6级所需经验值
    public final static int STATUS_ZERO = 0;  //状态值(0)
    public final static int STATUS_ONE = 1;  //状态值(1)
    public final static int GRADE = 1; //默认用户等级
    public final static int CODE_SIZE = 6; //验证码位数
    public final static int USER_FREQUENT_LIMIT = 5; //用户输入用户名密码错误次数
    public final static String USER_MESSAGE = "未知"; //用户默认信息
    public final static String USER_SIGNATURE = "这个人很懒,什么都没有写。"; //用户默认签名
    public final static String USER_NICK = "用户";  //用户昵称前缀
    public final static String USER_AVATAR = "https://ts1.cn.mm.bing.net/th/id/" +     //用户默认头像
            "R-C.baa2d1577cf82ad47aa705957237bff6?rik=IciTRLgFHnunww&riu=http%3a%2f%2fwww" +
            ".sucaijishi.com%2fuploadfile%2f2016%2f0203%2f20160203022632945.png&ehk=4PjlDtyiv" +
            "WKN7CP%2b5JlcgUiuD1nyme8IaxbNbPnANIw%3d&risl=&pid=ImgRaw&r=0";
    public final static String USER_CODE = "woha:user:code:"; //注册验证码
    public final static String EMAIL_FORM = "2572277647@qq.com";  //邮箱发送者
    public final static String STRATEGY_TYPE_LIST = "woha:strategy:type"; //攻略分类
    public final static String PICTURES_DETAIL = "woha:pictures:detail:"; //图片详情
    public final static String STORY_DETAIL = "woha:story:detail:";  //故事详情
    public final static String STRATEGY_DETAIL = "woha:strategy:detail:";  //攻略详情
    public final static String PICTURES_LIKED = "woha:pictures:liked:";  //图片点赞的用户
    public final static String STORY_LIKED = "woha:story:liked:";  //故事点赞的用户
    public final static String STRATEGY_LIKED = "woha:strategy:liked:";  //攻略点赞的用户
    public final static String COMMENT_LIKED = "woha:comment:liked";  //评论点赞的用户
    public final static int SEARCH_SIZE = 100000000;
    /*
    * 过期时间
    * */
    public final static int TIME_MIN = 1;
    public final static int TIME_SMALL = 60;
    public final static int TIME_BIG = 180;
    public final static int TIME_MAX = 1800;
    public final static int TIME_CODE = 300;
    public final static int TIME_STARTEGY = 7200;
    public final static int TYPE_ONE = 1;
    public final static int TYPE_TWO = 2;
    public final static int TYPE_THREE = 3;
}
