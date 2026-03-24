#!/bin/bash
# ================================================================
# 智能会议系统 - 服务管理工具
# 用法: bash service-ctl.sh {start|stop|restart|status|logs} [service-name]
# 示例: bash service-ctl.sh status
#       bash service-ctl.sh restart conference-auth
#       bash service-ctl.sh logs conference-gateway
# ================================================================

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

LOG_DIR="/opt/conference/logs"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

case "$1" in
    start)
        if [ -n "$2" ]; then
            echo "启动 $2..."
            systemctl start "$2"
            sleep 2
            systemctl status "$2" --no-pager | head -5
        else
            echo "启动所有服务..."
            for svc_port in "${SERVICES[@]}"; do
                SVC="${svc_port%%:*}"
                echo -n "  启动 $SVC..."
                systemctl start "$SVC"
                sleep 2
                if systemctl is-active --quiet "$SVC"; then
                    echo -e " ${GREEN}OK${NC}"
                else
                    echo -e " ${RED}FAIL${NC}"
                fi
            done
        fi
        ;;
    stop)
        if [ -n "$2" ]; then
            echo "停止 $2..."
            systemctl stop "$2"
        else
            echo "停止所有服务..."
            for svc_port in "${SERVICES[@]}"; do
                SVC="${svc_port%%:*}"
                echo "  停止 $SVC..."
                systemctl stop "$SVC"
            done
        fi
        ;;
    restart)
        if [ -n "$2" ]; then
            echo "重启 $2..."
            systemctl restart "$2"
            sleep 3
            systemctl status "$2" --no-pager | head -5
        else
            echo "重启所有服务..."
            for svc_port in "${SERVICES[@]}"; do
                SVC="${svc_port%%:*}"
                echo -n "  重启 $SVC..."
                systemctl restart "$SVC"
                sleep 3
                if systemctl is-active --quiet "$SVC"; then
                    echo -e " ${GREEN}OK${NC}"
                else
                    echo -e " ${RED}FAIL${NC}"
                fi
            done
        fi
        ;;
    status)
        echo "=============================="
        echo "  智能会议系统 - 服务状态"
        echo "=============================="
        for svc_port in "${SERVICES[@]}"; do
            SVC="${svc_port%%:*}"
            PORT="${svc_port##*:}"
            if systemctl is-active --quiet "$SVC"; then
                PID=$(systemctl show -p MainPID "$SVC" | cut -d= -f2)
                MEM=$(ps -o rss= -p "$PID" 2>/dev/null | awk '{printf "%.0fMB", $1/1024}')
                echo -e "  ${GREEN}✅${NC} $SVC:$PORT  PID:$PID  内存:$MEM"
            else
                echo -e "  ${RED}❌${NC} $SVC:$PORT  未运行"
            fi
        done
        echo ""
        echo "Nginx:  $(systemctl is-active nginx)"
        echo "MySQL:  $(systemctl is-active mysql 2>/dev/null || systemctl is-active mysqld 2>/dev/null)"
        echo "Redis:  $(systemctl is-active redis 2>/dev/null || systemctl is-active redis-server 2>/dev/null)"
        ;;
    logs)
        if [ -n "$2" ]; then
            echo "查看 $2 日志 (Ctrl+C退出)..."
            tail -f "$LOG_DIR/$2.log"
        else
            echo "用法: $0 logs <service-name>"
            echo "可用服务:"
            for svc_port in "${SERVICES[@]}"; do
                echo "  ${svc_port%%:*}"
            done
        fi
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|logs} [service-name]"
        echo ""
        echo "命令说明:"
        echo "  start   [svc]  - 启动(全部/指定)服务"
        echo "  stop    [svc]  - 停止(全部/指定)服务"
        echo "  restart [svc]  - 重启(全部/指定)服务"
        echo "  status         - 查看所有服务状态"
        echo "  logs    <svc>  - 查看指定服务日志"
        ;;
esac
