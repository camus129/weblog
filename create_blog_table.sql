USE weblog;

CREATE TABLE IF NOT EXISTS tb_blog (
  id bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id bigint(20) unsigned NOT NULL COMMENT '用户id',
  title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  images varchar(2048) NOT NULL COMMENT '文章图片，最多9张，多张以","隔开',
  content varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  liked int(8) unsigned zerofill DEFAULT '00000000' COMMENT '点赞数量',
  comments int(8) unsigned zerofill DEFAULT NULL COMMENT '评论数量',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;
