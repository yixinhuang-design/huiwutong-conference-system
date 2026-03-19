# SQLTools 执行数据库建表完整指南

> **快速开始**：5分钟内通过SQLTools执行排座系统建表脚本

---

## 📋 目录
1. SQLTools安装与配置
2. 数据库连接配置
3. 执行建表脚本（2种方法）
4. 验证建表结果
5. 常见问题解决
6. 快速操作表

---

## 1️⃣ SQLTools 安装与配置

### 步骤1.1：安装SQLTools扩展

在VS Code中：
1. 按 `Ctrl+Shift+X` 打开扩展市场
2. 搜索 **SQLTools** （ID: `mtxr.sqltools`）
3. 点击 **安装**

### 步骤1.2：安装MySQL驱动

1. 搜索 **SQLTools MySQL/MariaDB** （ID: `mtxr.sqltools-driver-mysql`）
2. 点击 **安装**

### 步骤1.3：验证安装

安装完成后，VS Code左侧边栏会出现 **SQLTools** 图标（数据库符号 🗄️）

---

## 2️⃣ 数据库连接配置

### 步骤2.1：打开连接配置

1. 点击左侧边栏 **SQLTools** 图标
2. 点击 **+ 创建新连接** 或点击 **Home** 中的 **创建新连接**

### 步骤2.2：选择MySQL

在驱动列表中选择 **MySQL**

### 步骤2.3：填写连接信息

| 参数 | 值 | 说明 |
|------|-----|------|
| **Connection Name** | `Local MySQL` | 连接别名（任意名称） |
| **Server Address** | `127.0.0.1` 或 `localhost` | MySQL服务器地址 |
| **Port** | `3308` | MySQL端口（注意不是3306！） |
| **Database** | 保留空白 | 初始数据库（先连接不指定） |
| **Username** | `root` | MySQL用户名 |
| **Password** | （你的密码） | MySQL密码 |
| **SSL** | `Off` | 关闭SSL |

### 步骤2.4：测试连接

1. 填写完成后，点击 **Test Connection**
2. 如果连接成功，会显示 **✅ Successfully connected** 
3. 点击 **Save Connection**

---

## 3️⃣ 执行建表脚本

### 方法一：直接执行完整脚本（推荐 ⭐）

**步骤3.1.1：打开建表脚本文件**

1. 在VS Code中打开文件：`seating_schema.sql`
2. 该文件位置：`conference-backend\conference-seating\src\main\resources\seating_schema.sql`

**步骤3.1.2：选择连接**

1. 脚本文件打开后，右下角会弹出 **Select Connection** 选择框
2. 选择 **Local MySQL**（刚创建的连接）

**步骤3.1.3：执行脚本**

在编辑器中：
- 按 `Ctrl+Shift+E` 快捷键执行整个脚本，**或**
- 右键点击文件 → **Run Query** → **Run all**

**预期输出**：
```
✅ Query successfully executed
✅ 9 rows affected (建表成功)
```

### 方法二：分段执行脚本（用于调试）

**步骤3.2.1：打开脚本文件**
同方法一

**步骤3.2.2：选择一部分脚本**

1. 用鼠标选中某一段SQL语句（例如第一个CREATE TABLE语句）
2. 右键 → **Run Query**

**步骤3.2.3：依次执行各段**

可以按以下顺序分段执行：
1. 创建数据库：`CREATE DATABASE IF NOT EXISTS conference_seating;`
2. 创建会场表：`CREATE TABLE conf_seating_venue ...`
3. 创建座位表：`CREATE TABLE conf_seating_seat ...`
4. 其他表...

---

## 4️⃣ 验证建表结果

### 方法一：SQLTools数据库浏览器（推荐 ⭐）

**步骤4.1.1：刷新连接**

1. 在SQLTools边栏中，找到 **Local MySQL** 连接
2. 点击刷新图标 🔄

**步骤4.1.2：展开数据库**

1. 找到 **conference_seating** 数据库
2. 点击展开箭头 ▶️
3. 查看 **Tables** 列表，应该看到9个表：
   - ✅ conf_seating_venue
   - ✅ conf_seating_seat
   - ✅ conf_seating_attendee
   - ✅ conf_seating_assignment
   - ✅ conf_seating_schedule
   - ✅ conf_seating_transport
   - ✅ conf_seating_accommodation
   - ✅ conf_seating_dining
   - ✅ conf_seating_assignment_history

**步骤4.1.3：查看表结构**

点击任意表 → 右键 → **Show Table Structure**，可查看字段定义

### 方法二：执行验证SQL脚本

**步骤4.2.1：打开验证脚本**

新建一个SQL文件，名为 `verify_seating_tables.sql`，内容为本文档后面的验证脚本

**步骤4.2.2：执行验证脚本**

1. 选择连接 **Local MySQL**
2. 按 `Ctrl+Shift+E` 执行

**步骤4.2.3：查看验证结果**

在 **OUTPUT** 面板中应该看到：
```
✅ 数据库已创建
✅ 表数量统计：9
✅ 字段总数统计：105
✅ 总索引数：30+
✅ 所有验证完成！
```

---

## 5️⃣ 常见问题解决

### 问题1：无法连接到数据库

**症状**：显示 `Connect ECONNREFUSED 127.0.0.1:3308`

**解决方案**：
1. 检查MySQL服务是否运行
   ```powershell
   # Windows PowerShell
   Get-Service | Where-Object {$_.Name -like "*MySQL*"}
   ```
2. 检查MySQL端口是否正确（应为3308，不是3306）
3. 尝试用命令行连接测试：
   ```bash
   mysql -h 127.0.0.1 -P 3308 -u root -p
   ```

### 问题2：连接成功但看不到数据库

**症状**：SQLTools中没有显示 `conference_seating` 数据库

**解决方案**：
1. 在SQLTools中右键 **Local MySQL** 连接
2. 点击 **Edit Connection** 
3. 将 **Database** 字段改为空（让SQLTools自动扫描）
4. 点击刷新

### 问题3：执行脚本显示"Access denied"

**症状**：`Access denied for user 'root'@'localhost'`

**解决方案**：
1. 检查连接配置中的密码是否正确
2. 尝试用命令行验证密码：
   ```bash
   mysql -h 127.0.0.1 -P 3308 -u root -p
   ```
3. 如果命令行可以连接但SQLTools不行，删除并重新创建SQLTools连接

### 问题4：脚本执行失败，显示"Syntax error"

**症状**：`You have an error in your SQL syntax`

**解决方案**：
1. 确保 `seating_schema.sql` 文件编码为 UTF-8
2. 确保整个脚本内容完整（检查文件大小约450行）
3. 尝试分段执行，定位具体出错的SQL语句

### 问题5：表已存在（重复执行脚本）

**症状**：`Table 'conference_seating.conf_seating_venue' already exists`

**解决方案**：
- 这是正常的，脚本中有 `CREATE TABLE IF NOT EXISTS` 保护
- 如果希望重新创建表：
  1. 右键 **conference_seating** 数据库
  2. 点击 **Drop Database**
  3. 再次执行脚本

---

## 6️⃣ 快速操作表

### 查看表数据

1. 在SQLTools边栏中找到表名（例如 `conf_seating_venue`）
2. 右键 → **Show Table Records**
3. 在新窗口中查看表数据（初始为空）

### 执行简单查询

**查看某表的所有数据**：
```sql
SELECT * FROM conf_seating_venue;
```

**统计表中的记录数**：
```sql
SELECT COUNT(*) as '记录数' FROM conf_seating_venue;
```

### 修改表结构

1. 右键表 → **Edit Table**
2. 在可视化编辑器中添加/修改列
3. 点击 **Save**

### 导入/导出数据

**导出为SQL**：
1. 右键表 → **Export** → **to SQL**

**导入CSV**：
1. 右键表 → **Import** → 选择CSV文件

---

## 📊 完整验证脚本

如需完整验证脚本，请执行以下SQL语句：

```sql
-- 1. 切换数据库
USE conference_seating;

-- 2. 验证表数量
SELECT COUNT(*) as '表数量' FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 3. 列出所有表
SHOW TABLES;

-- 4. 查看会场表结构
DESC conf_seating_venue;

-- 5. 查看座位表结构  
DESC conf_seating_seat;

-- 6. 统计所有字段数
SELECT COUNT(*) as '字段总数' FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 7. 统计索引数
SELECT COUNT(DISTINCT INDEX_NAME) as '索引总数' 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'conference_seating' AND INDEX_NAME != 'PRIMARY';
```

---

## ✅ 执行清单

按以下顺序完成：

- [ ] 1. 安装 SQLTools 扩展
- [ ] 2. 安装 SQLTools MySQL 驱动
- [ ] 3. 配置 MySQL 连接（Local MySQL）
- [ ] 4. 测试连接成功
- [ ] 5. 打开 seating_schema.sql 文件
- [ ] 6. 执行建表脚本（Ctrl+Shift+E）
- [ ] 7. 刷新 SQLTools 连接
- [ ] 8. 验证 9 个表已创建
- [ ] 9. 执行验证脚本（verify_seating_tables.sql）
- [ ] 10. 确认所有验证通过

---

## 🎯 预期结果

执行完成后，您应该看到：

✅ **conference_seating 数据库已创建**  
✅ **9 个表已创建**：
  - conf_seating_venue（会场配置）
  - conf_seating_seat（座位管理）
  - conf_seating_attendee（参会人员）
  - conf_seating_assignment（座位分配）
  - conf_seating_schedule（日程管理）
  - conf_seating_transport（车辆安排）
  - conf_seating_accommodation（住宿安排）
  - conf_seating_dining（用餐安排）
  - conf_seating_assignment_history（分配历史）

✅ **105 个字段已创建**  
✅ **30+ 个索引已创建**  
✅ **3 个外键约束已生效**  

**数据库准备就绪，可以启动后端应用！**

---

## 📞 需要帮助？

如果遇到问题：
1. 查看 **SQLTools 输出面板**（View → Output → SQLTools）获取详细错误信息
2. 参考上面的"常见问题解决"部分
3. 检查连接配置中的服务器地址和端口

---

**完成时间**：预计5分钟 ⏱️  
**下一步**：修改 `application.yml` 并启动后端应用
