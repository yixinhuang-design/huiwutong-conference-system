#!/bin/bash
# 批量优化huiwutong-uniapp页面的脚本

PROJECT_DIR="/tmp/huiwutong-conference-system/huiwutong-uniapp"
PAGES_DIR="$PROJECT_DIR/pages"

# Emoji到Font Awesome的映射
declare -A ICON_MAP
ICON_MAP["📅"]="fa-calendar-alt"
ICON_MAP["🪑"]="fa-th-large"
ICON_MAP["✅"]="fa-qrcode"
ICON_MAP["📚"]="fa-book-open"
ICON_MAP["👥"]="fa-users"
ICON_MAP["📒"]="fa-address-book"
ICON_MAP["📖"]="fa-book"
ICON_MAP["📝"]="fa-edit"
ICON_MAP["💬"]="fa-comments"
ICON_MAP["🔔"]="fa-bell"
ICON_MAP["✏️"]="fa-pen"
ICON_MAP["📸"]="fa-camera"
ICON_MAP["🔍"]="fa-search"
ICON_MAP["👤"]="fa-user"
ICON_MAP["⚙️"]="fa-cog"
ICON_MAP["📍"]="fa-map-marker-alt"
ICON_MAP["🏢"]="fa-building"
ICON_MAP["📞"]="fa-phone"
ICON_MAP["✨"]="fa-star"
ICON_MAP["📋"]="fa-clipboard"
ICON_MAP["🧭"]="fa-location-arrow"
ICON_MAP["🎯"]="fa-bullseye"

echo "开始批量优化..."
echo "项目目录: $PROJECT_DIR"
echo ""

# 统计
TOTAL_FILES=0
MODIFIED_FILES=0

# 遍历所有vue文件
find "$PAGES_DIR" -name "*.vue" -type f | while read file; do
    TOTAL_FILES=$((TOTAL_FILES + 1))
    
    # 检查文件是否包含emoji
    if grep -q "📅\|🪑\|✅\|📚\|👥\|📒\|📖\|📝\|💬\|🔔\|✏️\|📸\|🔍\|👤\|⚙️\|📍\|🏢\|📞\|✨\|📋\|🧭" "$file"; then
        echo "需要优化: $file"
        MODIFIED_FILES=$((MODIFIED_FILES + 1))
    fi
done

echo ""
echo "统计："
echo "总文件数: $TOTAL_FILES"
echo "需要优化: $MODIFIED_FILES"
echo ""
echo "建议使用手动优化以获得最佳质量"
