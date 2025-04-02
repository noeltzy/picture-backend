CREATE TABLE imageAiTask (
                             taskId BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
                             startTime DATETIME NOT NULL COMMENT '任务开始时间',
                             endTime DATETIME DEFAULT NULL COMMENT '任务结束时间',
                             url VARCHAR(255) NOT NULL COMMENT '任务相关URL',
                             status VARCHAR(20) DEFAULT 'Running' COMMENT '任务状态（默认：执行中）',
                             userId BIGINT NOT NULL COMMENT '用户ID',
                             INDEX idx_userId (userId)  -- 给 userId 添加索引
);
