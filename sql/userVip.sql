-- 2. 创建用户VIP表
CREATE TABLE userVip (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
                         userId BIGINT NOT NULL COMMENT '用户ID',
                         vipLevel INT NOT NULL DEFAULT 0 COMMENT 'VIP等级：0-免费用户 1-VIP1 2-VIP2 3-VIP3',
                         totalTokens BIGINT NOT NULL COMMENT 'Token总额度',
                         usedTokens BIGINT DEFAULT 0 COMMENT '已使用Token数量',
                         dailyLimit INT NOT NULL COMMENT '每日可调用次数',
                         startTime DATETIME NOT NULL COMMENT '生效时间',
                         endTime DATETIME NOT NULL COMMENT '到期时间',
                         status TINYINT DEFAULT 1 COMMENT '状态：0-已失效 1-生效中 2-已过期',
                         createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         orderNo CHAR(20) NOT NULL COMMENT '订单号(固定20位)',
                         updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                         INDEX idx_user_status (userId, status),
                         INDEX idx_end_time (endTime)
) COMMENT '用户VIP表' collate = utf8mb4_unicode_ci;

-- 4. 创建VIP订单表（可选，用于记录VIP购买订单）
CREATE TABLE vip_order (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
                          userId BIGINT NOT NULL COMMENT '用户ID',
                          orderNo CHAR(20) NOT NULL COMMENT '订单号(固定20位)',
                          vipLevel INT NOT NULL COMMENT 'VIP等级',
                          amount INT NOT NULL COMMENT '支付金额(单位:分)',
                          payStatus TINYINT DEFAULT 0 COMMENT '支付状态：0-未支付 1-已支付 2-已取消',
                          payTime DATETIME DEFAULT NULL COMMENT '支付时间',
                          createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                          UNIQUE KEY uk_order_no (orderNo),
                          INDEX idx_user_status (userId, payStatus)
) COMMENT 'VIP订单表' collate = utf8mb4_unicode_ci;

-- 在vip_order表中添加remark字段
ALTER TABLE vip_order
    ADD COLUMN remark varchar(255) DEFAULT NULL COMMENT '订单备注' AFTER payTime;