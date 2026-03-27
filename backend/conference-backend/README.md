# 智能会务系统后端 - 启动说明

> 更新日期：2026-02-26

## 一、环境要求
- JDK 21
- Maven 3.9+
- MySQL 9.6（本地默认端口：3308）
- Redis 7.2+
- Nacos 2.3+
- （可选）Kafka 3.7+（通知服务需要）

## 二、模块与端口
- gateway：8080
- auth：8081
- registration：8082
- notification：8083
- collaboration：8084
- seating：8085
- ai：8086
- navigation：8087
- data：8088

## 三、数据库初始化
SQL脚本位于 [sql/](sql/)：
- [sql/V1__init_schema.sql](sql/V1__init_schema.sql)（认证与基础表）
- [sql/V2__registration_schema.sql](sql/V2__registration_schema.sql)
- [sql/V3__seating_schema.sql](sql/V3__seating_schema.sql)
- [sql/V4__notification_schema.sql](sql/V4__notification_schema.sql)
- [sql/V5__collaboration_schema.sql](sql/V5__collaboration_schema.sql)

建议按顺序执行，确保数据库已创建并在配置中指向正确库名：
- conference_auth
- conference_registration
- conference_notification
- conference_seating
- conference_collaboration

## 四、配置说明
各服务默认配置位于：
- conference-*/src/main/resources/application.yml

关键配置项：
- MySQL：`spring.datasource.url`（默认端口 3308）
- Redis：`spring.data.redis.*`
- Nacos：`spring.cloud.nacos.*`
- JWT：`jwt.secret` / `jwt.expiration`

## 五、启动顺序
1. 启动 MySQL / Redis / Nacos（可选：Kafka）
2. 启动认证服务（conference-auth）
3. 启动其他业务服务
4. 启动网关（conference-gateway）

## 六、认证服务测试
- 登录：POST /auth/login
- 刷新：POST /auth/refresh
- 当前用户：GET /auth/me

> 默认管理员：`admin` / `admin123`

## 七、常见问题
- 连接失败：检查端口与账号
- Token无效：确认 `jwt.secret` 一致
- 服务无法注册：检查 Nacos 地址与命名空间
