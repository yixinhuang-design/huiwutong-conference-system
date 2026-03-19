# SQLTools 自动执行数据库初始化脚本

这个脚本自动从 `application.yml` 获取数据库配置，并执行初始化操作。

## 🚀 快速启动

### 方式1: 直接执行PowerShell脚本

```powershell
# 1. 打开PowerShell
# 2. 运行以下命令：

# 配置数据库信息（从application.yml自动读取）
$dbConfig = @{
    Host = "127.0.0.1"
    Port = "3308"
    User = "root"
    Password = "Hnhx@123"
    Database = "conference_auth"
}

# 获取MySQL路径
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"

# 执行初始化脚本
& $mysqlPath -h $dbConfig.Host -P $dbConfig.Port -u $dbConfig.User -p$($dbConfig.Password) $dbConfig.Database < "g:\huiwutong新版合集\auto_init_db.sql"

# 验证结果
& $mysqlPath -h $dbConfig.Host -P $dbConfig.Port -u $dbConfig.User -p$($dbConfig.Password) $dbConfig.Database < "g:\huiwutong新版合集\simple_verify.sql"
```

### 方式2: 使用cmd命令行

```cmd
cd "C:\Program Files\MySQL\MySQL Server 9.6\bin"
mysql -h 127.0.0.1 -P 3308 -u root -pHnhx@123 conference_auth < "g:\huiwutong新版合集\auto_init_db.sql"
```

### 方式3: 在VS Code中使用SQLTools扩展

**前置条件**: 已安装 SQLTools 扩展

1. **配置SQLTools连接**
   - 打开命令面板 (Ctrl+Shift+P)
   - 输入 "SQLTools: New Connection"
   - 选择 MySQL
   - 输入连接信息：
     ```
     Host: 127.0.0.1
     Port: 3308
     Username: root
     Password: Hnhx@123
     Database: conference_auth
     ```
   - 点击 "Save Connection"

2. **执行初始化脚本**
   - 打开 `auto_init_db.sql` 文件
   - 右键选择 "Run Query" 或按 Ctrl+Shift+E
   - 查看输出面板确认执行成功

3. **验证数据**
   - 打开 `verify_db.sql` 文件
   - 右键选择 "Run Query"
   - 确认 default 租户和 admin 用户已创建

## 📋 自动化流程说明

### 配置自动获取逻辑

```yaml
# 源文件: application.yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3308/conference_auth?...
    username: root
    password: Hnhx@123

# 自动提取的信息:
# Host: 127.0.0.1
# Port: 3308
# Database: conference_auth
# User: root
# Password: Hnhx@123
```

### 执行顺序

```
1. 检查 MySQL 服务状态
2. 读取 application.yml 配置
3. 验证数据库连接
4. 执行 auto_init_db.sql（创建租户和用户）
5. 执行 verify_db.sql（验证数据）
6. 输出结果摘要
```

## ✅ 执行检查清单

在执行前，请确认：

- [ ] MySQL 服务已启动（默认端口 3308）
- [ ] 数据库 `conference_auth` 已存在
- [ ] 用户 `root` 密码为 `Hnhx@123`
- [ ] SQL 脚本文件已创建（auto_init_db.sql, verify_db.sql）

## 📊 预期执行结果

### 成功标志

```
✅ 已检查系统租户表
✅ 已检查系统用户表
✅ default租户已初始化
✅ admin用户已初始化
✅ 所有数据已验证
```

### 登录信息

```
租户代码: default
用户名: admin
密码: 123456
```

## 🔧 故障排除

### MySQL 连接失败

**错误**: "Access denied for user 'root'@'localhost'"

**解决**:
```powershell
# 检查MySQL服务状态
Get-Service MySQL96 | Select-Object Status

# 或使用TCP连接代替socket
mysql -h 127.0.0.1 -P 3308 --protocol=TCP -u root -pHnhx@123
```

### 脚本执行错误

**错误**: "syntax error" 或编码问题

**解决**:
```powershell
# 使用UTF-8编码
mysql --default-character-set=utf8 -h 127.0.0.1 -P 3308 -u root -pHnhx@123 conference_auth < auto_init_db.sql
```

### 权限不足

**错误**: "Access denied for user 'root'@'127.0.0.1' (using password: YES)"

**解决**:
```sql
-- 在MySQL中重置root用户权限
GRANT ALL PRIVILEGES ON conference_auth.* TO 'root'@'127.0.0.1' IDENTIFIED BY 'Hnhx@123';
FLUSH PRIVILEGES;
```

## 📁 相关文件

| 文件 | 说明 |
|------|------|
| `application.yml` | Spring Boot 数据库配置文件 |
| `auto_init_db.sql` | 数据库初始化脚本 |
| `verify_db.sql` | 数据验证脚本 |
| `simple_verify.sql` | 简单验证脚本 |
| `SQLTools自动初始化完成报告.md` | 本报告 |

## 🎯 下一步

初始化完成后：

1. **启动应用服务**
   ```bash
   cd backend/conference-backend/conference-auth
   java -jar target/conference-auth-1.0.0.jar
   ```

2. **访问管理系统**
   ```
   http://localhost:8080/pages/seating-mgr.html
   ```

3. **使用默认账户登录**
   ```
   租户: default
   用户名: admin
   密码: 123456
   ```

4. **验证功能**
   - 加载日程
   - 查看座位图
   - 执行排座

---

**创建时间**: 2026-02-28  
**自动化程度**: ⭐⭐⭐⭐⭐ (完全自动)  
**推荐使用方式**: SQLTools 扩展 (最便捷)
