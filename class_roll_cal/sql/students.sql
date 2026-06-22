-- 课堂点名系统 - 数据库建表脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS class_roll_call
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE class_roll_call;

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_no   VARCHAR(50)  NOT NULL COMMENT '学号',
    name         VARCHAR(100) NOT NULL COMMENT '姓名',
    call_count   INT          DEFAULT 0 COMMENT '被点名次数',
    answer_count INT          DEFAULT 0 COMMENT '回答正确次数',
    enabled      TINYINT      DEFAULT 1 COMMENT '是否启用(1启用/0禁用)',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';
