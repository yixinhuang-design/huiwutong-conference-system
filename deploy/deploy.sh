#!/bin/bash
# ================================================================
# 智能会议系统 - 一键部署脚本
# 服务器: 39.103.85.255 (阿里云 + 宝塔面板)
# 用法: bash deploy.sh
# ================================================================

set -e

# ===== 配置区 =====
APP_NAME="conference"
APP_HOME="/opt/conference"
FRONTEND_HOME="/www/wwwroot/conference-admin"
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
SPRING_PROFILE="prod"
MYSQL_USER="conference"
MYSQL_PASSWORD="Conference@2026"
REDIS_HOST="127.0.0.1"

# 服务列表 (名称:端口)
SERVICES=(
    "conference-gateway:8080"
    "conference-auth:8081"
    "conference-registration:8082"
    "conference-notification:8083"
    "conference-meeting:8084"
    "conference-ai:8085"
    "conference-seating:8086"
    "conference-data:8088"
    "conference-collaboration:8089"
)

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "\n${BLUE}========== $1 ==========${NC}"; }

# ===== 步骤1: 检查和安装系统依赖 =====
install_dependencies() {
    log_step "步骤1: 检查系统环境"
    
    # 检查Java 21
    if command -v java &> /dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
        if [ "$JAVA_VER" -ge 21 ]; then
            log_info "Java 21+ 已安装: $(java -version 2>&1 | head -1)"
        else
            log_warn "Java版本过低($JAVA_VER), 需要安装Java 21"
            install_java21
        fi
    else
        log_warn "Java未安装, 正在安装Java 21..."
        install_java21
    fi
    
    # 检查MySQL
    if command -v mysql &> /dev/null; then
        log_info "MySQL 已安装: $(mysql --version)"
    else
        log_error "MySQL 未安装! 请通过宝塔面板安装MySQL 5.7/8.0"
        exit 1
    fi
    
    # 检查Redis
    if command -v redis-cli &> /dev/null; then
        log_info "Redis 已安装: $(redis-cli --version)"
    else
        log_warn "Redis 未安装, 正在安装..."
        yum install -y redis 2>/dev/null || apt-get install -y redis-server 2>/dev/null
        systemctl enable redis
        systemctl start redis
        log_info "Redis 安装完成"
    fi
    
    # 检查Nginx
    if command -v nginx &> /dev/null; then
        log_info "Nginx 已安装: $(nginx -v 2>&1)"
    else
        log_error "Nginx 未安装! 请通过宝塔面板安装Nginx"
        exit 1
    fi
}

install_java21() {
    log_info "下载并安装 OpenJDK 21..."
    
    # 方式1: 使用yum/dnf
    if command -v dnf &> /dev/null; then
        dnf install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null && return 0
    elif command -v yum &> /dev/null; then
        yum install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null && return 0
    elif command -v apt-get &> /dev/null; then
        apt-get update && apt-get install -y openjdk-21-jdk 2>/dev/null && return 0
    fi
    
    # 方式2: 手动安装
    log_info "包管理器安装失败，使用手动安装..."
    JAVA_DIR="/usr/local/java"
    mkdir -p $JAVA_DIR
    
    cd /tmp
    wget -q "https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz" -O jdk21.tar.gz || {
        # 备用: Adoptium
        wget -q "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse" -O jdk21.tar.gz
    }
    
    tar -xzf jdk21.tar.gz -C $JAVA_DIR
    JAVA_EXTRACTED=$(ls -d $JAVA_DIR/jdk-21* | head -1)
    
    # 设置环境变量
    cat > /etc/profile.d/java21.sh << EOF
export JAVA_HOME=$JAVA_EXTRACTED
export PATH=\$JAVA_HOME/bin:\$PATH
EOF
    source /etc/profile.d/java21.sh
    
    log_info "Java 21 安装完成: $(java -version 2>&1 | head -1)"
}

# ===== 步骤2: 创建目录结构 =====
setup_directories() {
    log_step "步骤2: 创建目录结构"
    
    mkdir -p $APP_HOME/{jars,logs,config,sql,scripts}
    mkdir -p $APP_HOME/ai-uploads
    mkdir -p $FRONTEND_HOME
    
    # 创建专用用户
    if ! id "$APP_NAME" &>/dev/null; then
        useradd -r -s /bin/false -d $APP_HOME $APP_NAME
        log_info "创建系统用户: $APP_NAME"
    fi
    
    chown -R $APP_NAME:$APP_NAME $APP_HOME
    log_info "目录创建完成: $APP_HOME"
}

# ===== 步骤3: 初始化数据库 =====
init_database() {
    log_step "步骤3: 初始化数据库"
    
    # 检查MySQL root密码
    MYSQL_ROOT_PWD=""
    if [ -f "$APP_HOME/config/.mysql_root_pwd" ]; then
        MYSQL_ROOT_PWD=$(cat "$APP_HOME/config/.mysql_root_pwd")
    fi
    
    if [ -z "$MYSQL_ROOT_PWD" ]; then
        read -sp "请输入MySQL root密码: " MYSQL_ROOT_PWD
        echo
        echo "$MYSQL_ROOT_PWD" > "$APP_HOME/config/.mysql_root_pwd"
        chmod 600 "$APP_HOME/config/.mysql_root_pwd"
    fi
    
    # 执行初始化SQL
    log_info "创建数据库和用户..."
    mysql -u root -p"$MYSQL_ROOT_PWD" < "$APP_HOME/sql/01-init-database.sql" 2>/dev/null && {
        log_info "数据库创建成功"
    } || {
        log_warn "数据库可能已存在，跳过创建"
    }
    
    # 导入各服务的迁移SQL
    for sql_dir in "$APP_HOME/sql/migrations"/*/; do
        DB_NAME=$(basename "$sql_dir")
        log_info "导入表结构: $DB_NAME"
        for sql_file in "$sql_dir"/*.sql; do
            if [ -f "$sql_file" ]; then
                mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" < "$sql_file" 2>/dev/null || {
                    log_warn "SQL导入跳过(表可能已存在): $(basename $sql_file)"
                }
            fi
        done
    done
    
    log_info "数据库初始化完成"
}

# ===== 步骤4: 部署后端JAR =====
deploy_backend() {
    log_step "步骤4: 部署后端服务"
    
    # 复制JAR文件
    for svc_port in "${SERVICES[@]}"; do
        SVC_NAME="${svc_port%%:*}"
        SVC_PORT="${svc_port##*:}"
        JAR_FILE="$APP_HOME/jars/${SVC_NAME}-1.0.0.jar"
        
        if [ -f "$JAR_FILE" ]; then
            log_info "✅ ${SVC_NAME} JAR已就位 (端口:${SVC_PORT})"
        else
            log_error "❌ 缺少: ${SVC_NAME}-1.0.0.jar"
        fi
    done
    
    chown -R $APP_NAME:$APP_NAME $APP_HOME/jars/
}

# ===== 步骤5: 安装systemd服务 =====
install_services() {
    log_step "步骤5: 安装systemd服务"
    
    for svc_port in "${SERVICES[@]}"; do
        SVC_NAME="${svc_port%%:*}"
        SVC_PORT="${svc_port##*:}"
        
        cat > /etc/systemd/system/${SVC_NAME}.service << EOF
[Unit]
Description=Conference ${SVC_NAME} Service
After=network.target mysql.service redis.service
Wants=mysql.service redis.service

[Service]
Type=simple
User=${APP_NAME}
Group=${APP_NAME}
WorkingDirectory=${APP_HOME}
Environment=JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
Environment=SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}
Environment=MYSQL_USER=${MYSQL_USER}
Environment=MYSQL_PASSWORD=${MYSQL_PASSWORD}
Environment=REDIS_HOST=${REDIS_HOST}
ExecStart=$(which java) ${JAVA_OPTS} -jar ${APP_HOME}/jars/${SVC_NAME}-1.0.0.jar --spring.profiles.active=${SPRING_PROFILE}
ExecStop=/bin/kill -TERM \$MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:${APP_HOME}/logs/${SVC_NAME}.log
StandardError=append:${APP_HOME}/logs/${SVC_NAME}.error.log
SuccessExitStatus=143
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
EOF
        
        systemctl daemon-reload
        systemctl enable ${SVC_NAME}
        log_info "已注册服务: ${SVC_NAME} (端口:${SVC_PORT})"
    done
}

# ===== 步骤6: 部署前端 =====
deploy_frontend() {
    log_step "步骤6: 部署前端"
    
    if [ -d "$APP_HOME/frontend" ]; then
        # 备份旧版本
        if [ -d "$FRONTEND_HOME" ] && [ "$(ls -A $FRONTEND_HOME)" ]; then
            BACKUP_DIR="${FRONTEND_HOME}.bak.$(date +%Y%m%d%H%M%S)"
            mv "$FRONTEND_HOME" "$BACKUP_DIR"
            mkdir -p "$FRONTEND_HOME"
            log_info "旧版本已备份到: $BACKUP_DIR"
        fi
        
        cp -r $APP_HOME/frontend/* $FRONTEND_HOME/
        chown -R www:www $FRONTEND_HOME
        log_info "前端文件已部署到: $FRONTEND_HOME"
    else
        log_warn "前端文件未找到: $APP_HOME/frontend/"
    fi
}

# ===== 步骤7: 配置Nginx =====
setup_nginx() {
    log_step "步骤7: 配置Nginx"
    
    NGINX_CONF="/www/server/panel/vhost/nginx/conference.conf"
    
    if [ -f "$APP_HOME/config/conference.conf" ]; then
        cp "$APP_HOME/config/conference.conf" "$NGINX_CONF"
        
        # 测试配置
        nginx -t && {
            nginx -s reload
            log_info "Nginx配置已生效"
        } || {
            log_error "Nginx配置有误，请检查!"
        }
    else
        log_warn "Nginx配置文件未找到"
    fi
}

# ===== 步骤8: 启动所有服务 =====
start_all_services() {
    log_step "步骤8: 启动所有服务"
    
    # 按依赖顺序启动
    ORDERED_SERVICES=(
        "conference-gateway"
        "conference-auth"
        "conference-registration"
        "conference-meeting"
        "conference-notification"
        "conference-seating"
        "conference-ai"
        "conference-collaboration"
        "conference-data"
    )
    
    for SVC in "${ORDERED_SERVICES[@]}"; do
        log_info "启动 ${SVC}..."
        systemctl start ${SVC}
        sleep 3
    done
    
    # 等待服务启动
    log_info "等待服务启动 (15秒)..."
    sleep 15
    
    # 检查状态
    echo ""
    log_step "服务状态检查"
    ALL_OK=true
    for svc_port in "${SERVICES[@]}"; do
        SVC_NAME="${svc_port%%:*}"
        SVC_PORT="${svc_port##*:}"
        
        if systemctl is-active --quiet ${SVC_NAME}; then
            echo -e "  ${GREEN}✅${NC} ${SVC_NAME}:${SVC_PORT} - 运行中"
        else
            echo -e "  ${RED}❌${NC} ${SVC_NAME}:${SVC_PORT} - 未启动"
            ALL_OK=false
        fi
    done
    
    echo ""
    if $ALL_OK; then
        log_info "🎉 所有服务启动成功!"
    else
        log_warn "部分服务未启动，请检查日志: $APP_HOME/logs/"
    fi
}

# ===== 步骤9: 最终检查 =====
final_check() {
    log_step "步骤9: 部署完成验证"
    
    echo ""
    echo "  📌 管理后台:   http://39.103.85.255"
    echo "  📌 API网关:    http://39.103.85.255/api/"
    echo "  📌 宝塔面板:   https://39.103.85.255:13319/5a6c6197"
    echo ""
    echo "  📋 常用命令:"
    echo "    查看服务状态: systemctl status conference-*"
    echo "    重启某个服务: systemctl restart conference-auth"
    echo "    查看日志:     tail -f $APP_HOME/logs/conference-auth.log"
    echo "    停止所有服务: systemctl stop conference-{gateway,auth,registration,meeting,notification,seating,ai,collaboration,data}"
    echo ""
}

# ===== 主流程 =====
main() {
    echo ""
    echo "========================================"
    echo "  智能会议系统 - 生产环境部署"
    echo "  服务器: 39.103.85.255"
    echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "========================================"
    echo ""
    
    install_dependencies
    setup_directories
    init_database
    deploy_backend
    install_services
    deploy_frontend
    setup_nginx
    start_all_services
    final_check
}

# 运行
main "$@"
