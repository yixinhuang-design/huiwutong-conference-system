#!/bin/bash
# Batch fix icon syntax errors in Vue files
# Replaces HTML strings with pure class names

cd "G:\huiwutong新版合集\huiwutong-uniapp\pages"

echo "开始批量修复图标语法..."
echo ""

files=(
  "archive/highlights.vue"
  "common/settings.vue"
  "common/profile.vue"
  "common/home.vue"
  "common/help.vue"
  "staff/tenant-detail.vue"
  "staff/task-feedback.vue"
  "staff/mobile-alert.vue"
  "staff/handbook-generate.vue"
  "staff/data-analysis.vue"
  "staff/dashboard.vue"
  "learner/seat-detail.vue"
  "learner/registration-status.vue"
  "learner/highlights.vue"
  "learner/evaluation.vue"
)

fixed_count=0

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo "处理: $file"

    # Fix HTML strings to class names
    sed -i "s/icon: '<text class=\"fa fa-check\"></text>'/icon: 'fa-check'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-clock\"></text>'/icon: 'fa-clock'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-calendar-alt\"></text>'/icon: 'fa-calendar-alt'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-th-large\"></text>'/icon: 'fa-th-large'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-book\"></text>'/icon: 'fa-book'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-book-open\"></text>'/icon: 'fa-book-open'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-address-book\"></text>'/icon: 'fa-address-book'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-comments\"></text>'/icon: 'fa-comments'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-users\"></text>'/icon: 'fa-users'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-camera\"></text>'/icon: 'fa-camera'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-chart-bar\"></text>'/icon: 'fa-chart-bar'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-chart-line\"></text>'/icon: 'fa-chart-line'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-tasks\"></text>'/icon: 'fa-tasks'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-bullhorn\"></text>'/icon: 'fa-bullhorn'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-microphone\"></text>'/icon: 'fa-microphone'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-edit\"></text>'/icon: 'fa-edit'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-cog\"></text>'/icon: 'fa-cog'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-user\"></text>'/icon: 'fa-user'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-bell\"></text>'/icon: 'fa-bell'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-search\"></text>'/icon: 'fa-search'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-home\"></text>'/icon: 'fa-home'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-qrcode\"></text>'/icon: 'fa-qrcode'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-star\"></text>'/icon: 'fa-star'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-star-o\"></text>'/icon: 'fa-star-o'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-lock\"></text>'/icon: 'fa-lock'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-mobile-alt\"></text>'/icon: 'fa-mobile-alt'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-eye\"></text>'/icon: 'fa-eye'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-eye-slash\"></text>'/icon: 'fa-eye-slash'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-map-marker-alt\"></text>'/icon: 'fa-map-marker-alt'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-file-alt\"></text>'/icon: 'fa-file-alt'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-download\"></text>'/icon: 'fa-download'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-handshake\"></text>'/icon: 'fa-handshake'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-clipboard-check\"></text>'/icon: 'fa-clipboard-check'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-chalkboard-teacher\"></text>'/icon: 'fa-chalkboard-teacher'/g" "$file"
    sed -i "s/icon: '<text class=\"fa fa-university\"></text>'/icon: 'fa-university'/g" "$file"

    echo "  ✓ 完成"
    ((fixed_count++))
  fi
done

echo ""
echo "================================"
echo "修复完成!"
echo "成功修复: $fixed_count 个文件"
echo "================================"
echo ""
echo "下一步：需要手动更新模板"
echo "将 {{ item.icon }} 改为 <text :class=\"item.icon\"></text>"
