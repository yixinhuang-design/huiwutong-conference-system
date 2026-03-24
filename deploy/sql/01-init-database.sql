-- ================================================================
-- 智能会议系统 - 数据库初始化脚本
-- 用途: 在生产服务器上创建所有数据库和用户
-- 执行: mysql -u root -p < init-database.sql
-- ================================================================

-- 创建专用数据库用户
CREATE USER IF NOT EXISTS 'conference'@'localhost' IDENTIFIED BY 'Conference@2026';
CREATE USER IF NOT EXISTS 'conference'@'127.0.0.1' IDENTIFIED BY 'Conference@2026';

-- 创建所有数据库
CREATE DATABASE IF NOT EXISTS `conference_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_registration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_notification` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_seating` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_collaboration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 授权
GRANT ALL PRIVILEGES ON `conference_auth`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_auth`.* TO 'conference'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `conference_registration`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_registration`.* TO 'conference'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `conference_notification`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_notification`.* TO 'conference'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `conference_seating`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_seating`.* TO 'conference'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `conference_collaboration`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_collaboration`.* TO 'conference'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `conference_db`.* TO 'conference'@'localhost';
GRANT ALL PRIVILEGES ON `conference_db`.* TO 'conference'@'127.0.0.1';

FLUSH PRIVILEGES;

SELECT '✅ 数据库和用户创建完成' AS status;
