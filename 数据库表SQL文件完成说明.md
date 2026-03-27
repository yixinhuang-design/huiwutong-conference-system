# 数据库表SQL文件完成说明

## 📋 创建时间
2026-03-24 20:30

## 🎯 创建内容

### 1️⃣ 主SQL文件
**文件路径**：
```
backend/conference-backend/conference-registration/src/main/resources/sql/registration_group_tables.sql
```

**包含内容**：
- ✅ 4张表的完整CREATE语句
- ✅ 索引优化建议
- ✅ 初始化数据示例
- ✅ 数据清理脚本
- ✅ 权限设置建议
- ✅ 数据完整性检查
- ✅ 性能优化建议

### 2️⃣ MyBatis Mapper XML文件

**文件1**：`RegistrationGroupMapper.xml`
- 路径：`src/main/resources/mapper/RegistrationGroupMapper.xml`
- 内容：分组表的CRUD映射

**文件2**：`RegistrationGroupMemberMapper.xml`
- 路径：`src/main/resources/mapper/RegistrationGroupMemberMapper.xml`
- 内容：分组成员表的CRUD映射

---

## 📊 数据库表结构

### 表1：registration_group（分组表）
```sql
CREATE TABLE registration_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conference_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(20),
    icon VARCHAR(50),
    type VARCHAR(20) DEFAULT 'manual',
    sort INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    creator_id BIGINT,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_conference (conference_id),
    INDEX idx_deleted (deleted),
    INDEX idx_sort (sort)
);
```

**字段说明**：
- `id` - 主键
- `conference_id` - 会议ID（外键）
- `name` - 分组名称
- `description` - 分组描述
- `color` - 分组颜色（用于前端展示）
- `icon` - 分组图标（Font Awesome类名）
- `type` - 分组类型（manual手动/auto自动）
- `sort` - 排序序号
- `deleted` - 软删除标记

### 表2：registration_group_member（分组成员表）
```sql
CREATE TABLE registration_group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    registration_id BIGINT NOT NULL,
    is_staff BOOLEAN DEFAULT FALSE,
    create_time DATETIME,
    UNIQUE KEY uk_group_member (group_id, registration_id),
    INDEX idx_group (group_id),
    INDEX idx_registration (registration_id),
    INDEX idx_staff (is_staff)
);
```

**字段说明**：
- `id` - 主键
- `group_id` - 分组ID（外键）
- `registration_id` - 报名ID（外键）
- `is_staff` - 是否为工作人员
- `create_time` - 加入分组时间

**约束**：
- `uk_group_member` - 唯一约束，同一分组中同一报名只能存在一次

### 表3：registration_audit_log（审核日志表）
```sql
CREATE TABLE registration_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id BIGINT NOT NULL,
    auditor_id BIGINT,
    auditor_name VARCHAR(50),
    action VARCHAR(20) NOT NULL,
    comment VARCHAR(500),
    create_time DATETIME,
    INDEX idx_registration (registration_id),
    INDEX idx_auditor (auditor_id),
    INDEX idx_create_time (create_time)
);
```

**字段说明**：
- `id` - 主键
- `registration_id` - 报名ID（外键）
- `auditor_id` - 审核人ID
- `auditor_name` - 审核人姓名
- `action` - 审核操作（approve通过/reject拒绝）
- `comment` - 审核意见

### 表4：registration_audit_rule（审核规则表）
```sql
CREATE TABLE registration_audit_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conference_id BIGINT NOT NULL,
    rule_type VARCHAR(20) NOT NULL,
    rule_content TEXT NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_conference (conference_id),
    INDEX idx_type (rule_type),
    INDEX idx_enabled (enabled),
    INDEX idx_priority (priority)
);
```

**字段说明**：
- `id` - 主键
- `conference_id` - 会议ID（外键）
- `rule_type` - 规则类型（keyword/blacklist/whitelist）
- `rule_content` - 规则内容（JSON格式）
- `rule_name` - 规则名称
- `enabled` - 是否启用
- `priority` - 优先级（数字越大优先级越高）

---

## 🚀 使用方法

### 方法1：直接执行SQL文件
```bash
# 进入MySQL
mysql -u root -p conference_registration

# 执行SQL文件
source /path/to/registration_group_tables.sql

# 或者使用命令行
mysql -u root -p conference_registration < registration_group_tables.sql
```

### 方法2：分步执行
```bash
# 1. 创建分组表
mysql -u root -p conference_registration < create_group_table.sql

# 2. 创建分组成员表
mysql -u root -p conference_registration < create_group_member_table.sql

# 3. 创建审核日志表
mysql -u root -p conference_registration < create_audit_log_table.sql

# 4. 创建审核规则表
mysql -u root -p conference_registration < create_audit_rule_table.sql
```

### 方法3：使用数据库管理工具
1. 打开Navicat/phpMyAdmin/MySQL Workbench
2. 选择数据库：`conference_registration`
3. 打开SQL文件：`registration_group_tables.sql`
4. 点击"运行"或"执行"

---

## ✅ 验证表是否创建成功

执行以下SQL检查：
```sql
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = 'conference_registration'
    AND TABLE_NAME IN (
        'registration_group',
        'registration_group_member',
        'registration_audit_log',
        'registration_audit_rule'
    )
ORDER BY 
    TABLE_NAME;
```

**预期结果**：
```
+-----------------------------+------------+---------------------+---------------------+
| TABLE_NAME                  | TABLE_ROWS | CREATE_TIME         | UPDATE_TIME         |
+-----------------------------+------------+---------------------+---------------------+
| registration_audit_log      |          0 | 2026-03-24 20:30:00 | NULL                |
| registration_audit_rule     |          2 | 2026-03-24 20:30:00 | 2026-03-24 20:30:00 |
| registration_group          |          0 | 2026-03-24 20:30:00 | NULL                |
| registration_group_member   |          0 | 2026-03-24 20:30:00 | NULL                |
+-----------------------------+------------+---------------------+---------------------+
```

---

## 📝 初始化数据说明

SQL文件中包含默认的审核规则示例：

### 规则1：测试账号过滤
```sql
INSERT INTO registration_audit_rule 
    (conference_id, rule_type, rule_content, rule_name, enabled, priority)
VALUES 
    (0, 'keyword', '{"keywords":["测试","test"],"action":"reject"}', 
     '测试账号过滤', TRUE, 10);
```

**作用**：自动拒绝姓名包含"测试"或"test"的报名

### 规则2：手机号格式验证
```sql
INSERT INTO registration_audit_rule 
    (conference_id, rule_type, rule_content, rule_name, enabled, priority)
VALUES 
    (0, 'whitelist', '{"phoneRegex":"^1[3-9]\\d{9}$","action":"approve"}', 
     '手机号格式验证', TRUE, 5);
```

**作用**：自动通过手机号格式正确的报名

---

## 🔧 索引优化说明

### 添加的索引
```sql
-- 复合索引
ALTER TABLE registration_group_member 
    ADD INDEX idx_group_staff (group_id, is_staff);

ALTER TABLE registration_audit_log 
    ADD INDEX idx_registration_time (registration_id, create_time);

ALTER TABLE registration_audit_rule 
    ADD INDEX idx_conference_enabled (conference_id, enabled);
```

**作用**：
- 提高查询性能
- 优化JOIN操作
- 加速排序和筛选

---

## 🧹 数据清理脚本

### 清空数据（保留表结构）
```sql
TRUNCATE TABLE registration_group_member;
TRUNCATE TABLE registration_group;
TRUNCATE TABLE registration_audit_log;
TRUNCATE TABLE registration_audit_rule;
```

### 删除表（慎用！）
```sql
DROP TABLE IF EXISTS registration_group_member;
DROP TABLE IF EXISTS registration_group;
DROP TABLE IF EXISTS registration_audit_log;
DROP TABLE IF EXISTS registration_audit_rule;
```

### 定期清理软删除数据
```sql
-- 删除90天前的软删除分组
DELETE FROM registration_group 
WHERE deleted = TRUE 
AND update_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

---

## 📊 性能优化建议

### 1. 定期归档审核日志
```sql
-- 创建归档表
CREATE TABLE registration_audit_log_archive LIKE registration_audit_log;

-- 归档1年前的日志
INSERT INTO registration_audit_log_archive
SELECT * FROM registration_audit_log
WHERE create_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- 删除已归档的数据
DELETE FROM registration_audit_log
WHERE create_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

### 2. 使用分区表（大数据量）
```sql
-- 按月分区审核日志表
ALTER TABLE registration_audit_log
PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
    PARTITION p202601 VALUES LESS THAN (202602),
    PARTITION p202602 VALUES LESS THAN (202603),
    -- ...
    PARTITION p_max VALUES LESS THAN MAXVALUE
);
```

---

## 🎯 总结

### 创建的文件
1. ✅ `registration_group_tables.sql` - 主SQL文件（6726字节）
2. ✅ `RegistrationGroupMapper.xml` - 分组Mapper（3254字节）
3. ✅ `RegistrationGroupMemberMapper.xml` - 成员Mapper（3995字节）

### 包含的表
1. ✅ `registration_group` - 分组表
2. ✅ `registration_group_member` - 分组成员表
3. ✅ `registration_audit_log` - 审核日志表
4. ✅ `registration_audit_rule` - 审核规则表

### 总计
- **4张表** - 完整的数据库结构
- **15个索引** - 优化查询性能
- **2个Mapper** - MyBatis映射配置
- **2条初始数据** - 审核规则示例

---

*SQL文件创建完成时间：2026-03-24 20:30*
*创建人员：AI Executive*
*数据库类型：MySQL 5.7+*
*字符集：utf8mb4_unicode_ci*
*存储引擎：InnoDB*
