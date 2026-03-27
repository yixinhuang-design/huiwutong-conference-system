# 后端 CORS 配置快速指南

## 📁 文件位置

将 `CorsConfig.java` 文件放到以下位置：

```
conference-gateway/
└── src/
    └── main/
        └── java/
            └── com/
                └── huiwutong/
                    └── gateway/
                        └── config/
                            └── CorsConfig.java  ← 放在这里
```

## 🚀 快速启用（3步）

### 步骤 1: 复制配置文件

将 `CorsConfig.java` 复制到 `conference-gateway/src/main/java/com/huiwutong/gateway/config/` 目录

### 步骤 2: 确认依赖

检查 `conference-gateway/pom.xml` 包含以下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**注意**: Gateway 项目默认应该已经包含此依赖，无需额外添加。

### 步骤 3: 重启服务

```bash
# 停止当前的 Gateway 服务
# 然后重新启动

# Maven 方式
cd conference-gateway
mvn spring-boot:run

# 或者在 IDE 中重启 GatewayApplication
```

## ✅ 验证配置

重启后，使用浏览器访问：

```
http://localhost:8080/api/auth/login
```

在浏览器开发者工具 → Network 中查看响应头，应该包含：

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## 🔧 生产环境配置

⚠️ **重要安全提示：**

当前配置使用 `allowedOriginPattern("*")` 允许所有来源，这仅适合开发环境。

**生产环境应该修改为：**

```java
// 仅允许特定域名
config.addAllowedOrigin("https://yourdomain.com");
config.addAllowedOrigin("https://admin.yourdomain.com");

// 或者使用正则匹配
config.addAllowedOriginPattern("https://*.yourdomain.com");
```

## 🧪 测试 CORS

使用 curl 测试预检请求：

```bash
curl -I -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:9066" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type, Authorization"
```

预期响应应包含：
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:9066
Access-Control-Allow-Methods: POST, GET, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: Content-Type, Authorization, X-Tenant-Id, X-User-Id
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## 🐛 故障排查

### 问题 1: 配置后仍然报 CORS 错误

**解决方案：**
1. 确认文件放在正确的 Gateway 模块中（不是其他微服务）
2. 确认已经重启了 Gateway 服务
3. 检查是否有其他 CORS 配置冲突

### 问题 2: 启动报错找不到类

**解决方案：**
1. 确认包名正确：`package com.huiwutong.gateway.config;`
2. 检查 pom.xml 是否包含 spring-boot-starter-webflux
3. 执行 `mvn clean compile` 重新编译

### 问题 3: 配置生效但前端仍然无法请求

**解决方案：**
1. 清除浏览器缓存（Ctrl + Shift + Delete）
2. 强制刷新前端页面（Ctrl + Shift + R）
3. 检查前端控制台的具体错误信息

## 📊 配置说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| allowedOriginPattern | * | 允许所有来源（开发环境） |
| allowedHeaders | * | 允许所有请求头 |
| allowedMethods | GET,POST,PUT,DELETE,OPTIONS,PATCH | 允许的 HTTP 方法 |
| allowCredentials | true | 允许携带凭证（Cookie、Token） |
| maxAge | 3600 | 预检请求缓存 1 小时 |

## 🔗 相关文档

- [Spring Cloud Gateway CORS 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration)
- [MDN CORS 文档](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS)

---

**配置完成后，前端应该可以正常调用后端 API，不再出现 CORS 错误。**
