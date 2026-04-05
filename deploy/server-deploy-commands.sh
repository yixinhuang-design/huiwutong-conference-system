#!/bin/bash
# ================================================================
# 服务器端部署命令 - 在服务器上执行
# ================================================================

echo "========================================"
echo "  智能会议系统 - 服务器端部署"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"
echo ""

# 步骤1: 解压部署包
echo "[步骤1] 解压部署包..."
cd /tmp
if [ -f conference-deploy-package.tar.gz ]; then
    echo "✅ 找到部署包: conference-deploy-package.tar.gz"
    tar xzf conference-deploy-package.tar.gz
    echo "✅ 解压完成"
else
    echo "❌ 未找到部署包"
    exit 1
fi

# 步骤2: 复制文件到目标目录
echo ""
echo "[步骤2] 复制文件到目标目录..."
mkdir -p /opt/conference/{jars,sql,config,scripts,frontend}

# 复制JAR文件
if [ -d upload-package/jars ]; then
    cp -f upload-package/jars/*.jar /opt/conference/jars/
    echo "✅ JAR文件已复制"
    ls -lh /opt/conference/jars/*.jar | awk '{printf "  %-50s %s\n", $9, $5}'
fi

# 复制SQL文件
if [ -d upload-package/sql ]; then
    cp -rf upload-package/sql/* /opt/conference/sql/
    echo "✅ SQL文件已复制"
fi

# 复制配置文件
if [ -d upload-package/config ]; then
    cp -f upload-package/config/* /opt/conference/config/
    echo "✅ 配置文件已复制"
fi

# 复制脚本文件
if [ -d upload-package/scripts ]; then
    cp -f upload-package/scripts/*.sh /opt/conference/scripts/
    chmod +x /opt/conference/scripts/*.sh
    echo "✅ 脚本文件已复制"
fi

# 复制前端文件
if [ -d upload-package/frontend ]; then
    # 备份旧版本
    if [ -d /www/wwwroot/conference-admin ] && [ "$(ls -A /www/wwwroot/conference-admin)" ]; then
        BACKUP_DIR="/www/wwwroot/conference-admin.bak.$(date +%Y%m%d%H%M%S)"
        mv /www/wwwroot/conference-admin "$BACKUP_DIR"
        echo "✅ 旧版本已备份到: $BACKUP_DIR"
    fi

    mkdir -p /www/wwwroot/conference-admin
    cp -rf upload-package/frontend/* /www/wwwroot/conference-admin/
    chown -R www:www /www/wwwroot/conference-admin
    echo "✅ 前端文件已部署"
fi

# 步骤3: 清理临时文件
echo ""
echo "[步骤3] 清理临时文件..."
rm -rf /tmp/upload-package
rm -f /tmp/conference-deploy-package.tar.gz
echo "✅ 临时文件已清理"

# 步骤4: 显示部署信息
echo ""
echo "[步骤4] 部署完成"
echo ""
echo "  📁 JAR文件: /opt/conference/jars/"
echo "  📁 SQL文件: /opt/conference/sql/"
echo "  📁 配置文件: /opt/conference/config/"
echo "  📁 脚本文件: /opt/conference/scripts/"
echo "  📁 前端文件: /www/wwwroot/conference-admin/"
echo ""
echo "  📋 下一步操作:"
echo "    1. 检查配置: cat /opt/conference/config/conference.conf"
echo "    2. 部署前端: cp /opt/conference/config/conference.conf /www/server/panel/vhost/nginx/"
echo "    3. 重启服务: bash /opt/conference/scripts/service-ctl.sh restart"
echo "    4. 检查状态: bash /opt/conference/scripts/service-ctl.sh status"
echo ""
echo "========================================"
echo "  部署文件准备完成！"
echo "========================================"
