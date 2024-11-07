-- ----------------------------
-- Table structure for champ_app_mgr_d
-- ----------------------------
--DROP TABLE IF EXISTS `champ_app_mgr_d`;
CREATE TABLE `champ_app_mgr_d`  (
    `RID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    RESU_IMG LONGBLOB null comment '资源图片',
    `object` enum('thread.message') DEFAULT NULL COMMENT 'thread.messageobject',
    `ss` enum('progress','incomplete','completed') DEFAULT NULL COMMENT '消息状态',
    `role` varbinary(64) DEFAULT NULL COMMENT '消息主题角色user或者assistant',
    PRIMARY KEY (`RID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

ALTER TABLE `champ_app_mgr_d`
    ADD COLUMN `MD5` varchar(32) NULL COMMENT '启动项内容MD5' AFTER `RID`,
    ADD COLUMN `STATUS` varchar(1) NULL COMMENT '是否为当前应用引用:0:否 1：是' AFTER `MD5`;

INSERT INTO champ_app_mgr_d (RID, RESU_IMG, MD5, STATUS) VALUES ('XXXXXX202008101823323310046028', 0xabcd, '0425aca53c9b73c68bcbc73449e3e71c', '1');
