# 批量emoji替换和样式优化脚本

## 说明
此脚本用于批量优化所有Vue页面，替换emoji为Font Awesome图标，并添加全局样式补丁引入。

## emoji到Font Awesome的映射表

```javascript
const emojiToFontAwesome = {
  // 登录相关
  '🔒': '<text class="fa fa-lock"></text>',
  '📱': '<text class="fa fa-mobile-alt"></text>',
  '👁️': '<text class="fa fa-eye"></text>',
  '👁️‍🗨️': '<text class="fa fa-eye-slash"></text>',
  '🔑': '<text class="fa fa-key"></text>',

  // 通知提醒
  '📢': '<text class="fa fa-bullhorn"></text>',
  '🔔': '<text class="fa fa-bell"></text>',
  '⏰': '<text class="fa fa-clock"></text>',
  '⏱️': '<text class="fa fa-stopwatch"></text>',

  // 数据图表
  '📊': '<text class="fa fa-chart-bar"></text>',
  '📈': '<text class="fa fa-chart-line"></text>',
  '📉': '<text class="fa fa-chart-line"></text>',

  // 位置导航
  '🏠': '<text class="fa fa-home"></text>',
  '📍': '<text class="fa fa-map-marker-alt"></text>',
  '🧭': '<text class="fa fa-compass"></text>',
  '🗺️': '<text class="fa fa-map"></text>',

  // 用户人员
  '👤': '<text class="fa fa-user"></text>',
  '👥': '<text class="fa fa-users"></text>',
  '👨‍💼': '<text class="fa fa-user-tie"></text>',
  '👩‍💼': '<text class="fa fa-user-tie"></text>',

  // 编辑操作
  '📝': '<text class="fa fa-edit"></text>',
  '✏️': '<text class="fa fa-pencil"></text>',
  '📄': '<text class="fa fa-file-alt"></text>',
  '📁': '<text class="fa fa-folder"></text>',
  '📂': '<text class="fa fa-folder-open"></text>',

  // 设置配置
  '⚙️': '<text class="fa fa-cog"></text>',
  '🔧': '<text class="fa fa-wrench"></text>',
  '💾': '<text class="fa fa-save"></text>',

  // 搜索查找
  '🔍': '<text class="fa fa-search"></text>',
  '🔎': '<text class="fa fa-search-plus"></text>',

  // 添加删除
  '➕': '<text class="fa fa-plus"></text>',
  '➖': '<text class="fa fa-minus"></text>',
  '❌': '<text class="fa fa-times"></text>',
  '✅': '<text class="fa fa-check"></text>',
  '✔️': '<text class="fa fa-check"></text>',

  // 状态评价
  '⭐': '<text class="fa fa-star"></text>',
  '🌟': '<text class="fa fa-star"></text>',
  '✨': '<text class="fa fa-star"></text>',
  '❤️': '<text class="fa fa-heart"></text>',
  '🤍': '<text class="fa fa-heart-o"></text>',
  '👍': '<text class="fa fa-thumbs-up"></text>',
  '👎': '<text class="fa fa-thumbs-down"></text>',

  // 警告注意
  '⚠️': '<text class="fa fa-exclamation-triangle"></text>',
  '❗': '<text class="fa fa-exclamation-circle"></text>',
  '📌': '<text class="fa fa-thumbtack"></text>',
  '🎯': '<text class="fa fa-bullseye"></text>',

  // 媒体相关
  '📷': '<text class="fa fa-camera"></text>',
  '🎤': '<text class="fa fa-microphone"></text>',
  '🎬': '<text class="fa fa-video"></text>',
  '📹': '<text class="fa fa-video"></text>',
  '🎵': '<text class="fa fa-music"></text>',

  // 通讯消息
  '💬': '<text class="fa fa-comments"></text>',
  '📨': '<text class="fa fa-envelope"></text>',
  '📧': '<text class="fa fa-envelope"></text>',
  '✉️': '<text class="fa fa-envelope"></text>',
  '📞': '<text class="fa fa-phone"></text>',
  '📱': '<text class="fa fa-mobile-alt"></text>',

  // 时间日期
  '📅': '<text class="fa fa-calendar-alt"></text>',
  '📆': '<text class="fa fa-calendar-alt"></text>',
  '🕐': '<text class="fa fa-clock"></text>',

  // 其他
  '🔙': '<text class="fa fa-arrow-left"></text>',
  '🔚': '<text class="fa fa-arrow-right"></text>',
  '🔄': '<text class="fa fa-sync"></text>',
  '🔃': '<text class="fa fa-sync"></text>',
  'ℹ️': '<text class="fa fa-info-circle"></text>',
  '❓': '<text class="fa fa-question-circle"></text>',
  '💡': '<text class="fa fa-lightbulb"></text>',
  '🎉': '<text class="fa fa-party-horn"></text>',
  '🏆': '<text class="fa fa-trophy"></text>',
  '📚': '<text class="fa fa-book"></text>',
  '📖': '<text class="fa fa-book-open"></text>',
  '🖨️': '<text class="fa fa-print"></text>',
  '📤': '<text class="fa fa-upload"></text>',
  '📥': '<text class="fa fa-download"></text>',
  '🔗': '<text class="fa fa-link"></text>',
  '🔗': '<text class="fa fa-chain"></text>',
  '🛒': '<text class="fa fa-shopping-cart"></text>',
  '🛍️': '<text class="fa fa-shopping-bag"></text>',
  '💳': '<text class="fa fa-credit-card"></text>',
  '💴': '<text class="fa fa-money-bill"></text>',
  '💵': '<text class="fa fa-money-bill"></text>',
  '🏧': '<text class="fa fa-atm"></text>'
}
```

## 使用方法

### 方法1：VS Code全局替换
1. 打开VS Code
2. 按 Ctrl+Shift+H 打开全局替换
3. 搜索范围选择"pages"文件夹
4. 文件类型选择".vue"
5. 逐个执行替换

### 方法2：PowerShell脚本
```powershell
# 设置路径
$path = "G:\huiwutong新版合集\huiwutong-uniapp\pages"

# 获取所有Vue文件
$files = Get-ChildItem -Path $path -Filter "*.vue" -Recurse

# 遍历每个文件
foreach ($file in $files) {
  $content = Get-Content $file.FullName -Raw -Encoding UTF8
  $modified = $false

  # 替换emoji（示例）
  if ($content -match '🔒') {
    $content = $content -replace '🔒', '<text class="fa fa-lock"></text>'
    $modified = $true
  }

  if ($content -match '➕') {
    $content = $content -replace '➕', '<text class="fa fa-plus"></text>'
    $modified = $true
  }

  # 添加global-patch引入
  if ($content -match '@import ''../../styles/common.scss''') {
    $content = $content -replace "@import '../../styles/common.scss';", "@import '../../styles/common.scss';`n@import '../../styles/global-patch.scss';"
    $modified = $true
  }

  # 保存修改
  if ($modified) {
    $content | Set-Content $file.FullName -Encoding UTF8 -NoNewline
    Write-Host "已优化: $($file.Name)" -ForegroundColor Green
  }
}
```

## 添加全局样式补丁引入

在每个Vue文件的`<style>`部分添加：
```scss
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss'; // 新增这行
```

## 验证优化效果

替换完成后，检查：
1. ✅ 所有emoji已替换为Font Awesome
2. ✅ 所有页面引入了global-patch.scss
3. ✅ 页面功能正常
4. ✅ 样式符合app原型
