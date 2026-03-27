# SQLTools 执行租户数据初始化指南

**创建日期**: 2026-02-27  
**工具**: VS Code SQLTools 扩展  
**数据库**: MySQL 9.6 (localhost:3308)  
**脚本**: 租户管理初始化脚本.sql

---

## 🎯 快速开始 (5 分钟)

### 步骤 1: 打开 SQL 脚本

在 VS Code 中打开：
```
G:\huiwutong新版合集\sql\租户管理初始化脚本.sql
```

### 步骤 2: 确保 SQLTools 连接已配置

1. 打开 VS Code 左侧栏 → **SQLTools** 图标
2. 查看 **DATABASES** 中是否有 "MySQL" 连接
3. 如果没有，点击 **+** 创建新连接：
   ```
   Host: localhost
   Port: 3308
   Username: root
   Password: (数据库密码)
   Database: conference_auth
   ```

### 步骤 3: 执行脚本

**方法 1: 直接执行全部**
```
按 Ctrl+Shift+Enter 执行整个文件
或点击右上角的执行按钮
```

**方法 2: 分段执行**
```
1. 选择一个 SELECT 语句
2. 按 Ctrl+Enter 执行
```

---

## 📋 脚本说明

### 脚本包含的操作

| 部分 | 说明 | 效果 |
|------|------|------|
| 1️⃣ 验证表 | 检查 sys_tenant 表是否存在 | 显示表结构 |
| 2️⃣ 清除数据 | (可选) 删除旧的测试数据 | 清空以重新初始化 |
| 3️⃣ 创建租户 | 初始化 5 个租户 | 多租户管理体系 |
| 4️⃣ 查询租户 | 验证租户创建成功 | 显示已创建租户 |
| 5️⃣ 创建用户 | 初始化管理员/协管员/学员 | 用户管理体系 |
| 6️⃣ 创建角色 | 初始化角色 | 权限管理体系 |
| 7️⃣ 查询用户和角色 | 验证创建成功 | 显示用户列表 |
| 8️⃣ 其他租户用户 | 为其他租户创建管理员 | 完整多租户支持 |
| 9️⃣ 数据统计 | 统计总数据量 | 数据量统计 |
| 🔟 完整性检查 | 检查数据一致性 | 验证数据完整 |
| 1️⃣1️⃣ 测试账号 | 显示可用的测试账号 | 方便后续登录测试 |
| 1️⃣2️⃣ 完成日志 | 显示执行完成 | 执行成功确认 |

---

## 💻 VS Code SQLTools 操作指南

### 打开 SQLTools 面板

```
方法 1: 点击左侧边栏 "SQLTools" 图标
方法 2: 快捷键 Ctrl+K Ctrl+C 打开命令面板，输入 "SQLTools"
方法 3: 右下角通知栏可能有快捷方式
```

### 执行 SQL 查询

```
【执行全部 SQL】
Ctrl+Shift+Enter

【执行当前行或选中的 SQL】
Ctrl+Enter

【执行到光标位置】
Ctrl+Alt+E

【打开查询编辑器】
Ctrl+Shift+P → "Run Query"
```

### 查看执行结果

```
执行后自动显示结果窗口:
├─ 右侧面板: 表格形式显示结果
├─ 下方面板: 详细信息和错误日志
└─ 终端: 执行时间和语句数
```

---

## 🔌 SQLTools 连接配置

### 如果还没有配置 MySQL 连接

1. **打开 SQLTools 设置**
   ```
   Ctrl+Shift+P → "SQLTools: Connections Manager"
   ```

2. **点击 "Create New Connection"**

3. **选择 MySQL**

4. **填写连接信息**
   ```
   Connection name: MySQL 9.6 (本地)
   Server address: localhost
   Port: 3308
   Username: root
   Password: (您的 MySQL 密码)
   Database: conference_auth
   ```

5. **点击 "Save Connection"**

6. **测试连接**
   ```
   右键点击连接 → "Test Connection"
   应显示 "Successfully connected"
   ```

---

## 📊 执行结果解读

### 成功执行的标志

```
✅ 如果看到以下信息，说明执行成功:

"✅ 租户初始化完成！"
"All tenant management data initialized successfully"

以及各个步骤的结果集显示
```

### 常见输出结果

```sql
-- 步骤 4: 创建的租户列表
| id | tenant_code  | tenant_name      | status |
|----|--------------|------------------|--------|
| 1  | tenant_001   | 智能会议系统...  | active |
| 2  | tenant_002   | 企业会议版本     | active |
| 3  | tenant_003   | 教育培训机构     | active |
| 4  | tenant_004   | 大型活动会议     | active |
| 5  | tenant_005   | 测试租户         | inactive |

-- 步骤 11: 可用的测试账号
| username | real_name | tenant_code | user_type |
|----------|-----------|-------------|-----------|
| admin    | 系统管理员 | tenant_001  | admin     |
| staff    | 协管员    | tenant_001  | staff     |
| learner  | 学员      | tenant_001  | learner   |
```

---

## 🧪 验证初始化是否成功

### 方法 1: 在 SQLTools 中查询

```sql
-- 快速验证
USE `conference_auth`;

-- 查看租户总数
SELECT COUNT(*) as total_tenants FROM sys_tenant;

-- 查看用户总数
SELECT COUNT(*) as total_users FROM sys_user;

-- 查看角色总数
SELECT COUNT(*) as total_roles FROM sys_role;
```

### 方法 2: 通过后端 API 验证

```bash
# 获取租户列表
curl -X GET "http://localhost:8081/api/tenant/list" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 登录测试
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 方法 3: 在前端登录测试

1. 打开浏览器：`http://localhost:8080/pages/login.html`
2. 使用测试账号登录
   ```
   用户名: admin
   密码: 123456
   ```
3. 如果成功登录，说明数据库初始化完成 ✅

---

## 🔧 故障排除

### 问题 1: SQLTools 连接失败

**症状**
```
"Connection failed"
"Can't connect to MySQL"
```

**解决方案**
```
1. 确保 MySQL 9.6 已启动
   - 任务管理器检查 "mysqld" 进程
   - 或运行: mysql -u root -p (检查是否能连接)

2. 检查连接信息
   - Host: localhost ✓
   - Port: 3308 (不是默认的 3306) ✓
   - Username: root ✓
   - Password: 正确 ✓

3. 重启 SQLTools
   - Ctrl+Shift+P → "Reload Window"
```

### 问题 2: 脚本执行时出现 SQL 错误

**症状**
```
"Error executing query"
"Table 'conference_auth.sys_tenant' doesn't exist"
```

**解决方案**
```
1. 确保 Flyway 已初始化数据库
   - 查看应用启动日志中是否有 "Flyway migrations completed"
   - 如果没有，需要先启动后端应用

2. 确保选择了正确的数据库
   - 检查 SQLTools 连接是否指向 conference_auth

3. 重新执行脚本
   - 等待 Flyway 完成后重新执行
```

### 问题 3: 违反唯一约束错误

**症状**
```
"Error: Duplicate entry 'tenant_001' for key 'uk_tenant_code'"
```

**解决方案**
```
1. 这是正常的！说明数据已存在

2. 脚本使用了 ON DUPLICATE KEY UPDATE
   - 如果数据已存在，会更新而不是插入
   - 这是设计好的行为

3. 如需清空重新初始化:
   - 取消注释脚本第 2 部分的 DELETE 语句
   - 然后重新执行整个脚本
```

### 问题 4: 超时错误

**症状**
```
"Query execution timeout"
"Too many rows"
```

**解决方案**
```
1. 分段执行而不是全部执行
   - 选择一个 SELECT 语句单独执行
   - 逐个执行不同部分

2. 增加 SQLTools 超时时间
   - File → Preferences → Settings
   - 搜索 "sqltools"
   - 增加 "queryTimeout" 值
```

---

## 📝 脚本自定义

### 修改租户信息

编辑脚本中的这部分 (第 3 部分):

```sql
-- 修改前
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, ...)
VALUES 
('tenant_001', '智能会议系统-主租户', '系统主要演示租户', 'active', ...);

-- 修改后
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, ...)
VALUES 
('tenant_001', '我的会议系统', '自定义租户名称', 'active', ...);
```

### 修改用户信息

编辑脚本中的这部分 (第 5 部分):

```sql
-- 修改前
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, ...)
VALUES 
(@tenant_id, 'admin', '$2a$10$...', 'admin@example.com', '13800138000', '系统管理员', 'admin', ...);

-- 修改后
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, ...)
VALUES 
(@tenant_id, 'admin', '$2a$10$...', 'newadmin@mycompany.com', '15800000000', '新管理员', 'admin', ...);
```

### 增加更多租户

在第 3 部分末尾添加:

```sql
-- 新增租户 6
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_006', '新租户', '新增租户描述', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();
```

---

## ✅ 完整检查清单

在运行脚本前，确保：

- [ ] MySQL 9.6 已启动
- [ ] SQLTools 已安装并配置好连接
- [ ] 应用已启动过一次 (Flyway 已初始化数据库)
- [ ] 有备份 (可选但推荐)
- [ ] 使用了正确的数据库 (conference_auth)

运行脚本后，验证：

- [ ] 没有 SQL 错误输出
- [ ] 显示 "✅ 租户初始化完成！"
- [ ] 所有租户都已创建 (COUNT = 5)
- [ ] 所有用户都已创建
- [ ] 所有角色都已创建
- [ ] 数据完整性检查通过

---

## 🎓 高级用法

### 导出查询结果到 CSV

```
1. 执行查询后
2. 在结果标签页右键 → "Export to CSV"
3. 选择保存位置
```

### 设置书签 (保存常用查询)

```
1. 编写查询语句
2. 右键 → "Bookmark this query"
3. 下次可从 SQLTools 的书签菜单快速访问
```

### 创建自动化脚本

```
多个 SQL 文件可以按顺序执行：
1. 创建多个 .sql 文件
2. 使用批处理脚本调用 mysql 命令行工具
3. 或在 CI/CD 流程中自动执行
```

---

## 📞 需要帮助?

### 检查点

1. **MySQL 是否运行?**
   ```powershell
   Get-Process mysqld
   ```

2. **SQLTools 是否已连接?**
   ```
   查看 SQLTools 面板左侧的连接状态
   应显示绿色的 "● MySQL" 或类似
   ```

3. **数据库是否存在?**
   ```sql
   SHOW DATABASES LIKE 'conference_%';
   ```

4. **脚本中是否有语法错误?**
   ```
   在 VS Code 中，SQL 关键字应该有语法高亮
   如果没有，可能有语法问题
   ```

---

## 📚 相关文档

- [租户管理初始化脚本.sql](../../sql/租户管理初始化脚本.sql) - SQL 脚本本体
- [SQLTools 官方文档](https://vscode-sqltools.mteixeira.dev/) - 工具文档
- [MySQL 官方文档](https://dev.mysql.com/doc/) - 数据库文档

---

**文档版本**: 1.0  
**最后更新**: 2026-02-27  
**维护状态**: ✅ 活跃

