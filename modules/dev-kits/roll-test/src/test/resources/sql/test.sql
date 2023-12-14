-- ----------------------------
-- Table structure for champ_app_mgr_d
-- ----------------------------
--DROP TABLE IF EXISTS `champ_app_mgr_d`;
CREATE TABLE `champ_app_mgr_d`  (
    `RID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用名称',
    `ALIAS` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '应用别名',
    `CATEGORY_ID` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类名称',
    `NAME_SPACE` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'namespace',
    `REPLICAS` int(11) DEFAULT NULL COMMENT '副本数',
    `DOMAIN` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '域名',
    `PORT` int(11) DEFAULT NULL COMMENT '端口',
    `PORT_TYPE` int(11) DEFAULT NULL COMMENT '端口类型',
    `CONTEXT_PATH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'context path',
    `YAML` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '应用配置信息',
    `CRTE_TIME` datetime(0) NOT NULL COMMENT '数据创建时间',
    `UPDT_TIME` datetime(0) NOT NULL COMMENT '数据更新时间',
    `CRTER_ID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建人ID',
    `CRTER_NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建人姓名',
    `OPTER_ID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经办人ID',
    `OPTER_NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经办人姓名',
    `OPT_TIME` datetime(0) NOT NULL COMMENT '经办时间',
    `OPTINS_NO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经办机构编号',
    `CRTE_OPTINS_NO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建机构编号',
    PRIMARY KEY (`RID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;
