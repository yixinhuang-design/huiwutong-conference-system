# SQLTools 执行数据库建表脚本 - 详细步骤

**日期**：2026年3月12日  
**任务**：通过 SQLTools 执行 seating_schema.sql 脚本  
**目标**：创建9张排座系统数据库表

---

## 一、SQLTools 连接配置

### 步骤1：打开 SQLTools 扩展

1. **快捷键**：按 `Ctrl+Shift+P` 打开命令面板
2. **输入**：`SQLTools: Open Connection Explorer`
3. **按 Enter** 打开连接浏览器

### 步骤2：创建新的 MySQL 连接

在 SQLTools Explorer 中：

1. **点击** "Add New Connection" (+ 图标)
2. **选择** "MySQL"
3. **填入以下信息**：

```
Connection Name:    conference_seating
Host:              127.0.0.1
Port:              3308
Username:          root
Password:          (您的MySQL密码)
Database:          (留空，稍后创建)
```

4. **点击** "Save Connection"
5. **点击** "Test Connection" 验证连接

### 步骤3：创建数据库

如果数据库 `conference_seating` 不存在，先创建它：

```sql
CREATE DATABASE IF NOT EXISTS `conference_seating` 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 二、执行建表脚本

### 方式1：直接在 SQLTools 中执行（推荐）

1. **在 VS Code 中打开** `seating_schema.sql` 文件
2. **选择所有内容**（Ctrl+A）
3. **右键点击** 或 **按 Ctrl+Shift+E**
4. **选择** "Run Query"
5. **选择连接**："conference_seating"
6. **等待执行完成** ✅

### 方式2：分段执行（推荐用于大脚本）

如果脚本太大，可以分段执行：

1. **选择第一段** SQL 代码（例如前5张表）
2. **右键** → **"Run Selected Query"**
3. **等待完成后**，选择下一段
4. **重复**直到全部执行完成

---

## 三、验证建表结果

### 验证方式1：在 SQLTools 中查看

1. **在 SQLTools Explorer 中**找到您的连接
2. **展开** "conference_seating" 数据库
3. **查看** "Tables" 部分
4. **应该看到 9 张表**：
   - conf_seating_venue
   - conf_seating_seat
   - conf_seating_attendee
   - conf_seating_assignment
   - conf_seating_schedule
   - conf_seating_transport
   - conf_seating_accommodation
   - conf_seating_dining
   - conf_seating_assignment_history

### 验证方式2：执行验证 SQL

在 SQLTools 中新建查询，执行以下 SQL：

```sql
-- 验证表是否创建成功
SHOW TABLES LIKE 'conf_seating_%';

-- 查看表数量
SELECT COUNT(*) as '表数量' FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 查看所有表和行数
SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating';
```

**预期结果**：
```
TABLE_NAME                              TABLE_ROWS
conf_seating_accommodation              0
conf_seating_assignment                 0
conf_seating_assignment_history         0
conf_seating_attendee                   0
conf_seating_dining                     0
conf_seating_schedule                   0
conf_seating_seat                       0
conf_seating_transport                  0
conf_seating_venue                      0
```

---

## 四、常见问题解决

### 问题1：SQLTools 无法连接到 MySQL

**错误**：`ECONNREFUSED 127.0.0.1:3308`

**解决**：
1. 确保 MySQL 已启动
2. 检查端口号是否正确（应为 3308）
3. 确保密码正确
4. 检查防火墙设置

### 问题2：权限错误

**错误**：`Access denied for user 'root'@'127.0.0.1'`

**解决**：
1. 检查用户名和密码
2. 使用 MySQL root 用户
3. 确保没有多余空格

### 问题3：字符集错误

**错误**：`Incorrect string value for column`

**解决**：
- 脚本已设置为 `utf8mb4_unicode_ci`
- 确保 MySQL 数据库使用 utf8mb4 字符集

### 问题4：某些表创建失败

**解决方式**：
1. 删除已创建的表：`DROP TABLE IF EXISTS table_name;`
2. 逐个执行建表语句，找出问题所在
3. 检查是否有语法错误

---

## 五、快速检查清单

执行后检查以下项目：

- [ ] MySQL 连接成功
- [ ] 数据库 `conference_seating` 已创建
- [ ] 9 张表全部创建成功
- [ ] 所有表的字段数正确（总共105个字段）
- [ ] 所有索引已创建（30+个）
- [ ] 没有错误或警告信息
- [ ] 可以执行 SELECT 查询

---

## 六、验证脚本完整示例

将以下脚本保存为 `verify_tables.sql`，在 SQLTools 中执行：

```sql
-- ============================================================
-- 排座系统数据库验证脚本
-- ============================================================

-- 1. 检查数据库是否存在
SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'conference_seating';

-- 2. 列出所有表
USE conference_seating;
SHOW TABLES;

-- 3. 统计表数量
SELECT COUNT(*) as 'Total_Tables' FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 4. 统计字段总数
SELECT COUNT(*) as 'Total_Columns' FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 5. 统计索引总数
SELECT COUNT(*) as 'Total_Indexes' FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'conference_seating' 
AND INDEX_NAME != 'PRIMARY';

-- 6. 检查每张表的结构
DESC conf_seating_venue;
DESC conf_seating_seat;
DESC conf_seating_attendee;
DESC conf_seating_assignment;
DESC conf_seating_schedule;
DESC conf_seating_transport;
DESC conf_seating_accommodation;
DESC conf_seating_dining;
DESC conf_seating_assignment_history;

-- 7. 验证外键约束
SELECT TABLE_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'conference_seating' AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================================
-- 执行完成后，所有表和索引应该已成功创建
-- ============================================================
```

---

## 七、SQLTools 快速操作

| 操作 | 快捷键 | 说明 |
|------|--------|------|
| 打开连接浏览器 | Ctrl+Shift+E | 查看数据库连接 |
| 执行查询 | Ctrl+Shift+J | 执行当前SQL |
| 格式化SQL | Shift+Alt+F | 美化SQL代码 |
| 打开命令面板 | Ctrl+Shift+P | SQLTools 各种命令 |

---

## 八、成功标志

✅ **数据库建表成功的标志**：

1. 9 张表都已创建
2. 所有外键约束都已生效
3. 所有索引都已创建
4. 可以执行基本的 SELECT 查询
5. 数据库字符集为 utf8mb4

---

## 九、后续步骤

建表完成后：

1. ✅ 修改 `application.yml` 数据库连接信息
2. ✅ 编译 Maven 项目
3. ✅ 启动 Spring Boot 应用
4. ✅ 测试 API 接口

---

## 十、支持和反馈

如有任何问题：

1. 查看 SQLTools 输出窗口获取详细错误信息
2. 检查 MySQL 错误日志
3. 验证脚本语法正确性
4. 确保数据库权限正确

---

**制定人**：AI Assistant  
**版本**：1.0  
**最后更新**：2026年3月12日
