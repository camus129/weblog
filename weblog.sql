-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: weblog
-- ------------------------------------------------------
-- Server version	8.0.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `content` varchar(10000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `cover_img` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章封面',
  `state` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT '草稿' COMMENT '文章状态: 只能是[已发布] 或者 [草稿]',
  `category_id` int unsigned DEFAULT NULL COMMENT '文章分类ID',
  `create_user` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `liked` int DEFAULT '0' COMMENT '点赞数',
  `collected` int DEFAULT '0' COMMENT '收藏数',
  PRIMARY KEY (`id`),
  KEY `fk_article_category` (`category_id`),
  KEY `fk_article_user` (`create_user`),
  CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `fk_article_user` FOREIGN KEY (`create_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `article_comment`
--

DROP TABLE IF EXISTS `article_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `article_id` bigint NOT NULL COMMENT '文章id',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论id',
  `user_id` bigint NOT NULL COMMENT '评论者id',
  `content` varchar(255) NOT NULL COMMENT '评论内容',
  `target_user_id` bigint DEFAULT '0' COMMENT '回复的目标用户id',
  `target_comment_id` bigint DEFAULT '0' COMMENT '回复的目标评论id',
  `reply_times` int NOT NULL DEFAULT '0' COMMENT '回复数量',
  `liked_times` int NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `hidden` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否被隐藏，默认false',
  `anonymity` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否匿名，默认false',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `category_name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_alias` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类别名',
  `create_user` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_category_user` (`create_user`),
  CONSTRAINT `fk_category_user` FOREIGN KEY (`create_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collected_record`
--

DROP TABLE IF EXISTS `collected_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collected_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `biz_id` bigint NOT NULL COMMENT '收藏的目标id',
  `biz_type` varchar(16) NOT NULL COMMENT '收藏的目标类型',
  `is_collected` tinyint(1) DEFAULT '1' COMMENT '是否收藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_biz_user` (`biz_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `liked_record`
--

DROP TABLE IF EXISTS `liked_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `liked_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `biz_id` bigint NOT NULL COMMENT '点赞的目标id',
  `biz_type` varchar(16) NOT NULL COMMENT '点赞的目标类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_liked` tinyint(1) DEFAULT '1' COMMENT '是否点赞',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_biz_user` (`biz_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2044325642576416770 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_follow`
--

DROP TABLE IF EXISTS `tb_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户id',
  `follow_user_id` bigint unsigned NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nick_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2041361854755622914 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'weblog'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-15 19:09:54
