/**
 * 前端静态文件服务器
 * 用法: node server.js
 * 访问: http://localhost:8000
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8000;
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
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Tenant-Id');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

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
    res.setHeader('Content-Type', contentType);
    res.writeHead(200);
    fs.createReadStream(filePath).pipe(res);
}

server.listen(PORT, '0.0.0.0', () => {
    console.log(`\n  前端开发服务器已启动`);
    console.log(`  ─────────────────────────────`);
    console.log(`  本地访问: http://localhost:${PORT}`);
    console.log(`  登录页面: http://localhost:${PORT}/pages/login.html`);
    console.log(`  排座管理: http://localhost:${PORT}/pages/seating-mgr.html`);
    console.log(`  服务目录: ${BASE_PATH}`);
    console.log(`  CORS:     已启用`);
    console.log(`  ─────────────────────────────`);
    console.log(`  按 Ctrl+C 停止服务器\n`);
});

// 防止进程意外退出
process.on('uncaughtException', (err) => {
    console.error('未捕获异常:', err.message);
});

process.on('SIGTERM', () => {
    console.log('收到SIGTERM信号，关闭服务器...');
    server.close(() => process.exit(0));
});

// 保持进程存活 - 每30秒心跳
setInterval(() => {}, 30000);
