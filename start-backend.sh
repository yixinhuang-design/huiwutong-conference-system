#!/bin/bash
# 认证服务启动脚本（Linux/Mac版本）

cd "$(dirname "$0")/backend/conference-backend"

echo "========================================="
echo "  认证服务启动"
echo "========================================="
echo ""

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "[✗] Maven未安装"
    exit 1
fi

echo "[✓] Maven已检测"
echo ""

# 编译并启动
echo "编译中... (约1-2分钟)"
mvn clean install -DskipTests -q

if [ $? -eq 0 ]; then
    echo "[✓] 编译成功"
    echo ""
    echo "启动认证服务 (http://localhost:8081)..."
    echo ""
    mvn -pl conference-auth -am spring-boot:run
else
    echo "[✗] 编译失败"
    exit 1
fi
