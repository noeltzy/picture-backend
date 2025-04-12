CREATE TABLE image_gen_task (
                             taskId BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
                             startTime DATETIME NOT NULL COMMENT '任务开始时间',
                             endTime DATETIME DEFAULT NULL COMMENT '任务结束时间',
                             url VARCHAR(255) NOT NULL COMMENT '任务相关URL',
                             status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态（默认：执行中）',
                             userId BIGINT NOT NULL COMMENT '用户ID',
                             INDEX idx_userId (userId)  -- 给 userId 添加索引
);

ALTER TABLE image_gen_task
    ADD COLUMN tokensUsed INT NOT NULL DEFAULT 0 COMMENT '本次任务消耗的token数量',
    ADD COLUMN vipLevel TINYINT UNSIGNED DEFAULT 0 COMMENT 'VIP等级：0-免费用户 1-VIP1 2-VIP2 3-VIP3',
    ADD COLUMN taskType TINYINT UNSIGNED DEFAULT 0 COMMENT '任务类型：0-生成图片 1-编辑图片',
    ADD COLUMN errorMessage VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    ADD COLUMN tokenRefunded TINYINT UNSIGNED DEFAULT 0 COMMENT 'token是否已退还：0-未退还 1-已退还 2-无需退还',
    ADD COLUMN refundTime DATETIME DEFAULT NULL COMMENT 'token退还时间',
    ADD COLUMN refundRemark VARCHAR(255) DEFAULT NULL COMMENT '退还备注';