USE weblog;

-- 添加 shop_id 字段到 tb_blog 表
ALTER TABLE tb_blog ADD COLUMN shop_id bigint(20) DEFAULT NULL COMMENT '商户id' AFTER id;
