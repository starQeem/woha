/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50739
Source Host           : localhost:3306
Source Database       : woha

Target Server Type    : MYSQL
Target Server Version : 50739
File Encoding         : 65001

Date: 2023-05-22 15:09:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for Comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `content` varchar(255) DEFAULT NULL COMMENT '评论内容',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `pictures_id` bigint(20) DEFAULT NULL COMMENT '图片id',
  `story_id` bigint(20) DEFAULT NULL COMMENT '故事id',
  `strategy_id` bigint(20) DEFAULT NULL COMMENT '攻略id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `parent_comment_id` bigint(20) DEFAULT NULL,
  `is_admin` int(11) DEFAULT '0' COMMENT '是否为楼主',
  `comment_nick_name` varchar(255) DEFAULT NULL COMMENT '评论区@的人的昵称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `follow_user_id` bigint(20) DEFAULT NULL COMMENT '用户关注的人的用户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Pictures
-- ----------------------------
DROP TABLE IF EXISTS `pictures`;
CREATE TABLE `pictures` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `pictures_address` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `title` varchar(255) DEFAULT NULL COMMENT '图片标题',
  `content` longtext COMMENT '图片介绍',
  `user_id` bigint(20) DEFAULT NULL,
  `views` int(11) DEFAULT NULL COMMENT '浏览次数',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '发布时间',
  `comment_count` int(20) DEFAULT '0' COMMENT '评论数',
  `liked` int(11) DEFAULT '0' COMMENT '点赞',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Story
-- ----------------------------
DROP TABLE IF EXISTS `story`;
CREATE TABLE `story` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` longtext COMMENT '文章内容',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `views` int(20) DEFAULT NULL COMMENT '浏览次数',
  `comment_count` int(20) DEFAULT '0' COMMENT '评论数',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '发布时间',
  `first_picture` varchar(255) DEFAULT NULL COMMENT '首图地址',
  `liked` int(11) DEFAULT '0' COMMENT '点赞',
  `description` varchar(255) DEFAULT NULL COMMENT '文章简介',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Strategy
-- ----------------------------
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type_id` bigint(11) DEFAULT NULL COMMENT '类型id',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` longtext COMMENT '内容',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `first_picture` varchar(255) DEFAULT NULL COMMENT '首图地址',
  `views` int(255) DEFAULT NULL COMMENT '浏览次数',
  `comment_count` int(255) DEFAULT '0' COMMENT '评论数',
  `liked` int(11) DEFAULT '0' COMMENT '点赞',
  `description` varchar(255) DEFAULT NULL COMMENT '文章简介',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for strategy_type
-- ----------------------------
DROP TABLE IF EXISTS `strategy_type`;
CREATE TABLE `strategy_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(255) DEFAULT NULL COMMENT '类型名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for User
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(100) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `sex` varchar(255) DEFAULT NULL COMMENT '性别',
  `signature` varchar(255) DEFAULT NULL COMMENT '签名',
  `nick_name` varchar(255) DEFAULT NULL COMMENT '用户昵称',
  `hobby` varchar(255) DEFAULT NULL COMMENT '爱好',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user_task
-- ----------------------------
DROP TABLE IF EXISTS `user_task`;
CREATE TABLE `user_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `dailytask_strategy` int(255) DEFAULT '0' COMMENT '每日任务,观看一篇攻略',
  `dailytask_story` int(255) DEFAULT '0' COMMENT '每日任务,观看一篇故事',
  `dailytask_login` int(255) DEFAULT '0' COMMENT '每日任务,登录',
  `weeklytask_pictures` int(255) DEFAULT '0' COMMENT '每周任务,发布一张美图',
  `experience` int(11) DEFAULT '0' COMMENT '用户经验',
  `grade` int(11) DEFAULT '1' COMMENT '等级',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
