#!/bin/bash
# 批量修复Font Awesome图标语法错误
# 将 data 中的 '<text class="fa fa-check"></text>' 改为 'fa-check'
# 并在模板中使用正确的 :class 绑定

cd "G:\huiwutong新版合集\huiwutong-uniapp\pages"

echo "开始批量修复Font Awesome图标语法..."
echo ""

# 需要修复的文件列表
files=(
  "archive/highlights.vue"
  "common/assistant.vue"
  "common/help.vue"
  "common/home.vue"
)

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo "处理: $file"

    # 1. 替换data中的icon定义
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
    sed -i "s/icon: '<text class=\"fa fa-tasks\"></text>'/icon: 'fa-tasks'/g" "$file"

    echo "  ✓ 已修复"
  fi
done

echo ""
echo "修复完成！"
echo ""
echo "注意事项："
echo "1. 检查模板中是否正确使用 :class 绑定"
echo "2. 示例：<text :class=\"item.icon\"></text>"
echo "3. 或者直接使用：<text class=\"fa fa-check\"></text>"