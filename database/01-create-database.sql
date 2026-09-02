CREATE DATABASE IF NOT EXISTS catmate
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE catmate;

-- 表结构由 Spring Boot JPA 在首次启动时创建和更新。
-- 演示账号和猫咪数据由后端 DataInitializer 自动写入。
SHOW TABLES;
