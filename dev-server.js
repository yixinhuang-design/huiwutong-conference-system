const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const STATIC_DIR = path.join(__dirname, 'admin-pc', 'conference-admin-pc');
const API_HOST = '127.0.0.1';
const API_PORT = 8080;
const SERVER_PORT = 3000;

const MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2',
    '.ttf': 'font/ttf',
};

const server = http.createServer((req, res) => {
    const parsed = url.parse(req.url);
    const pathname = parsed.pathname;

    // API proxy: /api/** -> localhost:8080/api/**
    if (pathname.startsWith('/api/') || pathname.startsWith('/ws/')) {
        const options = {
            hostname: API_HOST,
            port: API_PORT,
            path: req.url,
            method: req.method,
            headers: { ...req.headers, host: `${API_HOST}:${API_PORT}` },
        };
        const proxyReq = http.request(options, (proxyRes) => {
            res.writeHead(proxyRes.statusCode, proxyRes.headers);
            proxyRes.pipe(res);
        });
        proxyReq.on('error', (e) => {
            res.writeHead(502);
            res.end(`API proxy error: ${e.message}`);
        });
        req.pipe(proxyReq);
        return;
    }

    // Static file serving
    let filePath = path.join(STATIC_DIR, pathname === '/' ? 'index.html' : pathname);
    
    // If no extension, try .html
    if (!path.extname(filePath) && !filePath.endsWith('/')) {
        if (fs.existsSync(filePath + '.html')) {
            filePath += '.html';
        } else if (fs.existsSync(path.join(filePath, 'index.html'))) {
            filePath = path.join(filePath, 'index.html');
        }
    }

    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404);
            res.end('Not Found: ' + pathname);
            return;
        }
        const ext = path.extname(filePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || 'application/octet-stream';
        res.writeHead(200, { 'Content-Type': contentType });
        res.end(data);
    });
});

server.listen(SERVER_PORT, () => {
    console.log(`\n========================================`);
    console.log(`  Local Dev Server running at:`);
    console.log(`  http://localhost:${SERVER_PORT}`);
    console.log(`  Static: ${STATIC_DIR}`);
    console.log(`  API proxy: localhost:${API_PORT}`);
    console.log(`========================================\n`);
});
