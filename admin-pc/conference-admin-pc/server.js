/**
 * 前端静态文件服务器（含API反向代理）
 * 用法: node server.js
 * 访问: http://localhost:8000
 * API代理: /api/* → http://localhost:8080/api/*
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8000;
const API_TARGET = 'http://localhost:8080'; // 后端Gateway地址
const BASE_PATH = __dirname;

const MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2',
    '.ttf': 'font/ttf',
    '.eot': 'application/vnd.ms-fontobject',
    '.map': 'application/json',
    '.mp4': 'video/mp4',
    '.mp3': 'audio/mpeg',
    '.pdf': 'application/pdf',
    '.zip': 'application/zip',
    '.txt': 'text/plain; charset=utf-8'
};

const server = http.createServer((req, res) => {
    // CORS headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, PATCH');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Tenant-Id');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    // === API反向代理: /api/* → Gateway(8080) ===
    if (req.url.startsWith('/api/')) {
        const targetUrl = new URL(req.url, API_TARGET);
        const proxyOptions = {
            hostname: targetUrl.hostname,
            port: targetUrl.port,
            path: targetUrl.pathname + targetUrl.search,
            method: req.method,
            headers: { ...req.headers, host: targetUrl.host }
        };

        const proxyReq = http.request(proxyOptions, (proxyRes) => {
            // 复制响应头并覆盖CORS
            const headers = { ...proxyRes.headers };
            headers['access-control-allow-origin'] = '*';
            headers['access-control-allow-methods'] = 'GET, POST, PUT, DELETE, OPTIONS, PATCH';
            headers['access-control-allow-headers'] = 'Content-Type, Authorization, X-Tenant-Id';
            res.writeHead(proxyRes.statusCode, headers);
            proxyRes.pipe(res, { end: true });
        });

        proxyReq.on('error', (err) => {
            console.error(`[API代理] 请求失败: ${req.method} ${req.url} → ${err.message}`);
            res.writeHead(502, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ code: 502, message: '后端服务不可用，请确保Gateway(8080)已启动' }));
        });

        // 超时设置
        proxyReq.setTimeout(30000, () => {
            proxyReq.destroy();
            res.writeHead(504, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ code: 504, message: '后端服务响应超时' }));
        });

        req.pipe(proxyReq, { end: true });
        return;
    }

    // === 静态文件服务 ===
    let urlPath = decodeURIComponent(req.url.split('?')[0]);
    let filePath = path.join(BASE_PATH, urlPath);

    // 目录请求 → index.html
    if (urlPath.endsWith('/')) {
        filePath = path.join(filePath, 'index.html');
    }

    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            // 如果是目录，尝试 index.html
            if (!err && stats.isDirectory()) {
                filePath = path.join(filePath, 'index.html');
                fs.stat(filePath, (err2, stats2) => {
                    if (err2 || !stats2.isFile()) {
                        res.writeHead(404, { 'Content-Type': 'text/plain' });
                        res.end('Not Found: ' + urlPath);
                        return;
                    }
                    serveFile(filePath, res);
                });
                return;
            }
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('Not Found: ' + urlPath);
            return;
        }
        serveFile(filePath, res);
    });
});

function serveFile(filePath, res) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    const stream = fs.createReadStream(filePath);
    stream.on('error', (err) => {
        console.error(`[静态文件] 读取失败: ${filePath} → ${err.message}`);
        if (!res.headersSent) {
            res.writeHead(500, { 'Content-Type': 'text/plain' });
        }
        res.end('Internal Server Error');
    });
    res.writeHead(200, { 'Content-Type': contentType });
    stream.pipe(res);
}

server.on('error', (err) => {
    console.error(`[服务器] 监听失败: ${err.message}`);
    if (err.code === 'EADDRINUSE') {
        console.error(`  端口 ${PORT} 被占用，请释放端口后重试`);
    }
    process.exit(1);
});

server.listen(PORT, () => {
    console.log(`\n  前端开发服务器已启动`);
    console.log(`  ─────────────────────────────`);
    console.log(`  本地访问: http://localhost:${PORT}`);
    console.log(`  登录页面: http://localhost:${PORT}/pages/login.html`);
    console.log(`  API代理:  /api/* → ${API_TARGET}`);
    console.log(`  服务目录: ${BASE_PATH}`);
    console.log(`  CORS:     已启用`);
    console.log(`  ─────────────────────────────`);
    console.log(`  按 Ctrl+C 停止服务器\n`);
});

// 防止进程意外退出
process.on('uncaughtException', (err) => {
    console.error('[uncaughtException]', err.stack || err.message);
});

process.on('unhandledRejection', (reason) => {
    console.error('[unhandledRejection]', reason);
});

process.on('SIGTERM', () => {
    console.log('收到SIGTERM信号，关闭服务器...');
    server.close(() => process.exit(0));
});

// 保持进程存活 - 每30秒心跳
setInterval(() => {}, 30000);
