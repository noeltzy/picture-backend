CREATE TABLE file_info (
                           id INT AUTO_INCREMENT PRIMARY KEY,          -- 文件的唯一标识符
                           fileName VARCHAR(255) NOT NULL,            -- 文件名
                           fileSize BIGINT NOT NULL,                  -- 文件大小（单位：字节）
                           fileHash CHAR(32) NOT NULL UNIQUE,     -- 文件哈希值（MD5、SHA256等）
                           file_url VARCHAR(255) NOT NULL,             -- 文件存储路径或URL地址
                           uploadCount INT DEFAULT 1,                 -- 文件上传次数（首次上传为1）
                           uploadTime DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 文件上传时间
                           INDEX (fileHash)                           -- 创建索引以便根据文件哈希快速查询
);
ALTER TABLE file_info
    MODIFY COLUMN id BIGINT AUTO_INCREMENT,        -- 修改id字段为BIGINT类型，并保持自增
    ADD COLUMN fileHeight INT,                      -- 添加文件高度字段
    ADD COLUMN fileWidth INT,                       -- 添加文件宽度字段
    ADD COLUMN fileFormat VARCHAR(50);              -- 添加文件格式字段


ALTER TABLE file_info
    CHANGE COLUMN file_url fileUrl VARCHAR(255) NOT NULL;
