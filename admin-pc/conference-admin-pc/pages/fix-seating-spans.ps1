#!/usr/bin/env pwsh
<#
修复seating-mgr.html中缺失的</span>标签
#>

$htmlFile = 'g:\huiwutong新版合集\admin-pc\conference-admin-pc\pages\seating-mgr.html'
$content = Get-Content -Path $htmlFile -Raw

# 将template部分和script部分分开
$scriptStartPos = $content.IndexOf('        const { createApp')
$templatePart = $content.Substring(0, $scriptStartPos)
$scriptPart = $content.Substring($scriptStartPos)

# 分析template部分的span平衡
$opens = [regex]::Matches($templatePart, '<span[^>]*(?<!/)>').Count
$closes = [regex]::Matches($templatePart, '</span>').Count
$missing = $opens - $closes

Write-Host "Template部分span分析:"
Write-Host "  打开: $opens"
Write-Host "  关闭: $closes"
Write-Host "  缺失: $missing"

if ($missing -gt 0) {
    # 找出最后一个打开span的位置
    $lastSpanMatch = $null
    foreach ($match in [regex]::Matches($templatePart, '<span[^>]*(?<!/)>', 'RightToLeft')) {
        $lastSpanMatch = $match
        break
    }
    
    if ($lastSpanMatch) {
        $insertPos = $lastSpanMatch.Index + $lastSpanMatch.Length
        
        # 生成缺失的关闭标签
        $closingTags = '</span>' * $missing
        
        # 找到插入位置之后最近的安全位置（比如在最后一个</div>之前）
        $divClosePos = $templatePart.LastIndexOf('</div>')
        if ($divClosePos -gt $insertPos) {
            $finalContent = $templatePart.Substring(0, $divClosePos) + $closingTags + $templatePart.Substring($divClosePos) + $scriptPart
        } else {
            $finalContent = $templatePart + $closingTags + $scriptPart
        }
        
        # 保存修复后的文件
        Set-Content -Path $htmlFile -Value $finalContent -Encoding UTF8
        Write-Host "已添加$missing个</span>标签到文件"
    }
} else {
    Write-Host "span标签已平衡，无需修复"
}
