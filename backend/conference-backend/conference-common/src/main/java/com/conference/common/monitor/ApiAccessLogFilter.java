package com.conference.common.monitor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * API 访问日志上报 Filter
 * 拦截所有 HTTP 请求，异步上报访问日志到数据监控服务 (conference-data:8088)
 * 放置在 conference-common 中，各服务通过 ApiAccessLogAutoConfig 自动注册
 */
@Slf4j
public class ApiAccessLogFilter implements Filter {

    private final String serviceName;
    private final String dataServiceUrl;
    private final HttpClient httpClient;
    private final ExecutorService executor;

    public ApiAccessLogFilter(String serviceName, String dataServiceUrl) {
        this.serviceName = serviceName;
        this.dataServiceUrl = dataServiceUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        // 单线程异步上报，避免阻塞业务请求
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "api-log-reporter");
            t.setDaemon(true);
            return t;
        });
        log.info("[ApiAccessLog] 已初始化 - 服务: {}, 上报地址: {}", serviceName, dataServiceUrl);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 跳过不需要记录的路径
        String uri = request.getRequestURI();
        if (shouldSkip(uri)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        long startTime = System.currentTimeMillis();

        // 先执行正常请求
        chain.doFilter(servletRequest, servletResponse);

        long elapsed = System.currentTimeMillis() - startTime;
        int statusCode = response.getStatus();
        String method = request.getMethod();

        // 异步上报，不阻塞业务
        executor.submit(() -> reportAccess(method, uri, statusCode, elapsed));
    }

    /**
     * 跳过无需记录的路径（静态资源、健康检查、上报接口自身等）
     */
    private boolean shouldSkip(String uri) {
        return uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/favicon") ||
               uri.startsWith("/actuator") ||
               uri.contains("/system/log-access") ||
               uri.contains("/system/log-event");
    }

    /**
     * 异步上报访问日志到 data 服务
     */
    private void reportAccess(String method, String path, int statusCode, long responseTime) {
        try {
            String json = String.format(
                    "{\"serviceName\":\"%s\",\"method\":\"%s\",\"path\":\"%s\",\"statusCode\":%d,\"responseTime\":%d}",
                    serviceName, method, path, statusCode, responseTime
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dataServiceUrl + "/api/data/system/log-access"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((resp, ex) -> {
                        if (ex != null) {
                            log.debug("[ApiAccessLog] 上报失败(data服务可能未启动): {}", ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.debug("[ApiAccessLog] 构建上报请求异常: {}", e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
