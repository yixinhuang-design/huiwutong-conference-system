# 后端启动问题诊断和解决

## 当前问题

Maven本地仓库中的POM文件损坏，导致无法编译：

```
[FATAL] Non-parseable POM spring-boot-dependencies-3.2.3.pom
```

## 解决方案

### 方案A：完全清除Maven缓存（推荐）

**Windows PowerShell**：

```powershell
# 1. 关闭所有Maven进程
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

# 2. 清除Maven仓库
Remove-Item -Path "$env:USERPROFILE\.m2\repository" -Recurse -Force
New-Item -ItemType Directory -Path "$env:USERPROFILE\.m2\repository" -Force

# 3. 重新编译（会自动下载依赖）
cd backend\conference-backend
mvn clean install -DskipTests

# 4. 启动认证服务
mvn -pl conference-auth -am spring-boot:run
```

**或使用bat脚本**：

创建 `fix-maven.bat`：

```batch
@echo off
echo 清除Maven缓存...
rmdir /S /Q "%USERPROFILE%\.m2\repository"
mkdir "%USERPROFILE%\.m2\repository"
echo 完成。现在可以重新编译。
```

### 方案B：使用阿里云镜像加速下载

编辑 `backend\conference-backend\maven-settings.xml`，添加镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

然后编译：

```bash
mvn -s maven-settings.xml clean install -DskipTests
```

### 方案C：跳过编译，使用预编译版本

如果已有编译好的JAR文件（target目录中），可以直接运行：

```bash
java -jar backend\conference-backend\conference-auth\target\conference-auth-1.0.0.jar
```

## 完整步骤

### 1. 清除缓存并重新编译

```powershell
# 进入后端目录
cd backend\conference-backend

# 清除所有缓存
mvn clean

# 清除本地仓库损坏的文件
Remove-Item -Path "$env:USERPROFILE\.m2\repository\org\springframework" -Recurse -Force

# 重新下载并编译（第一次需要5-10分钟）
mvn install -DskipTests -X

# 或者使用静默模式（看不到进度）
mvn install -DskipTests -q
```

### 2. 验证编译是否成功

```bash
# 检查认证模块的JAR文件
ls backend\conference-backend\conference-auth\target\*.jar

# 应该看到类似: conference-auth-1.0.0.jar
```

### 3. 启动认证服务

**选项A：使用spring-boot:run插件**

```bash
cd backend\conference-backend
mvn -pl conference-auth -am spring-boot:run
```

**选项B：直接运行JAR**

```bash
java -jar backend\conference-backend\conference-auth\target\conference-auth-1.0.0.jar \
  --server.port=8081 \
  --spring.datasource.url=jdbc:mysql://127.0.0.1:3308/conference_auth \
  --spring.datasource.username=root \
  --spring.datasource.password=Hnhx@123
```

## 预期输出

编译成功的标志：

```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

启动成功的标志：

```
2026-02-26 HH:MM:SS.mmm  INFO  AuthApplication : Starting AuthApplication
2026-02-26 HH:MM:SS.mmm  INFO  AuthApplication : Started AuthApplication in X.XXX seconds (JVM running for Y.YYY)
Server running at http://localhost:8081
```

## 快速检查清单

- [ ] Maven已安装: `mvn --version`
- [ ] Java已安装: `java --version`
- [ ] MySQL已运行: `netstat -ano | findstr :3308`
- [ ] 后端目录存在: `dir backend\conference-backend`
- [ ] pom.xml存在: `dir backend\conference-backend\pom.xml`

## 如果仍然失败

### 检查1：网络连接

```bash
# 测试Maven仓库连接
ping repo.maven.apache.org
ping maven.aliyun.com
```

### 检查2：Java版本要求

```bash
java --version
# 需要 Java 21 或更高版本
```

### 检查3：Maven版本

```bash
mvn --version
# 需要 Maven 3.8 或更高版本
```

### 检查4：磁盘空间

```bash
# Windows需要至少5GB用于Maven缓存
dir C:\ | find "free"
```

## 替代方案：Docker

如果Maven编译持续失败，可以使用Docker：

```dockerfile
# Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/conference-backend .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /app/conference-auth/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

启动Docker容器：

```bash
docker build -t conference-auth .
docker run -p 8081:8081 -e SPRING_DATASOURCE_PASSWORD=Hnhx@123 conference-auth
```

## 临时修复脚本

创建 `quick-start-backend.bat`：

```batch
@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0backend\conference-backend"

echo 步骤1: 清除缓存...
mvn clean -q

echo 步骤2: 下载依赖...
mvn dependency:resolve -q

echo 步骤3: 编译...
mvn compile -q

echo 步骤4: 启动服务...
mvn -pl conference-auth -am spring-boot:run

pause
```

## 获取更多帮助

- Maven文档: https://maven.apache.org/
- Spring Boot: https://spring.io/projects/spring-boot
- 常见问题: CORS_SOLUTION.md
- 快速测试: QUICK_TEST_GUIDE.md
