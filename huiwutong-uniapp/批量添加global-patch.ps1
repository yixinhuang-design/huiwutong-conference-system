# PowerShell脚本：批量添加global-patch.scss引入到所有Vue文件

$path = "G:\huiwutong新版合集\huiwutong-uniapp\pages"
$modifiedCount = 0
$errorCount = 0

Write-Host "开始批量添加global-patch.scss引入..." -ForegroundColor Cyan
Write-Host "目标路径: $path" -ForegroundColor Yellow
Write-Host ""

# 获取所有Vue文件
$files = Get-ChildItem -Path $path -Filter "*.vue" -Recurse

Write-Host "找到 $($files.Count) 个Vue文件" -ForegroundColor Cyan
Write-Host ""

# 遍历每个文件
foreach ($file in $files) {
  try {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    # 检查是否已包含global-patch引入
    if ($content -match 'global-patch\.scss') {
      Write-Host "跳过: $($file.Name) - 已包含global-patch" -ForegroundColor DarkGray
      continue
    }

    # 查找common.scss引入位置
    if ($content -match "@import '\.\./\.\./styles/common\.scss';") {
      # 在common.scss后添加global-patch引入
      $newContent = $content -replace "@import '\.\./\.\./styles/common\.scss';", "@import '../../styles/common.scss';`n@import '../../styles/global-patch.scss';"

      # 保存修改
      $newContent | Set-Content $file.FullName -Encoding UTF8 -NoNewline
      Write-Host "✓ 已优化: $($file.Name)" -ForegroundColor Green
      $modifiedCount++
    }
    else {
      Write-Host "! 警告: $($file.Name) - 未找到common.scss引入" -ForegroundColor Yellow
    }
  }
  catch {
    Write-Host "✗ 错误: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
    $errorCount++
  }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "批量优化完成！" -ForegroundColor Green
Write-Host "修改文件数: $modifiedCount" -ForegroundColor Green
Write-Host "错误文件数: $errorCount" -ForegroundColor Red
Write-Host "======================================" -ForegroundColor Cyan
