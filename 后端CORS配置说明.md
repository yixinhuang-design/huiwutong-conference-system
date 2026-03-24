# 后端 CORS 跨域配置说明

## 问题描述

前端 H5 开发环境 (`localhost:9066`) 访问后端 API (`localhost:8080`) 时遇到 CORS 跨域错误：

```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login' from origin 'http://localhost:9066'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

## 解决方案

### 方案一：配置后端网关 CORS（推荐）

在 `conference-gateway` 服务的配置文件中添加 CORS 配置：

#### 1. application.yml 配置

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

#### 2. 或使用 Java 配置类

在 `conference-gateway/src/main/java/` 下创建：

```java
package com.huiwutong.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的来源
        config.addAllowedOriginPattern("*");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

### 方案二：使用 Nginx 反向代理

如果使用 Nginx，添加以下配置：

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    location / {
        root /path/to/huiwutong-uniapp/dist/build/h5;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # CORS 配置
        add_header 'Access-Control-Allow-Origin' '*';
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS';
        add_header 'Access-Control-Allow-Headers' '*';
        add_header 'Access-Control-Allow-Credentials' 'true';

        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }
}
```

### 方案三：Chrome 开发模式（仅用于测试）

⚠️ **警告：此方法仅用于本地开发，不要用于生产环境！**

1. 关闭所有 Chrome 窗口

2. 以管理员权限运行命令提示符，执行：

```bash
# Windows
chrome.exe --user-data-dir="C:/chrome-dev" --disable-web-security --disable-features=BlockInsecurePrivateNetworkRequests

# 或创建快捷方式，目标添加：
"C:\Program Files\Google\Chrome\Application\chrome.exe" --user-data-dir="C:/chrome-dev" --disable-web-security --disable-features=BlockInsecurePrivateNetworkRequests"
```

3. 使用这个特殊的 Chrome 窗口进行开发

## 验证 CORS 是否生效

重启后端服务后，使用 curl 测试：

```bash
curl -I -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:9066" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"
```

如果看到以下响应头说明 CORS 配置成功：

```
Access-Control-Allow-Origin: http://localhost:9066
Access-Control-Allow-Methods: POST, GET, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization, X-Tenant-Id, X-User-Id
Access-Control-Allow-Credentials: true
```

## 前端已配置的代理

前端 `manifest.json` 已配置开发环境代理：

```json
{
  "h5": {
    "devServer": {
      "proxy": {
        "/api": {
          "target": "http://localhost:8080",
          "changeOrigin": true
        }
      }
    }
  }
}
```

但某些后端服务可能仍需要 CORS 配置才能正常工作。

## 推荐配置

**最佳实践：**
1. ✅ 开发环境：前端代理 + 后端 CORS（双重保障）
2. ✅ 生产环境：Nginx 反向代理 + 后端 CORS

**快速测试：**
1. 在网关的 `application.yml` 添加方案一的配置
2. 重启后端服务
3. 刷新前端页面测试登录

---

**注意：** 如果后端服务已经添加了 CORS 配置但仍然报错，请检查：
- 是否重启了后端服务
- 配置是否在正确的 Gateway 服务中（不是其他微服务）
- 防火墙是否阻止了请求
- 后端服务是否正常运行在 8080 端口
