# Batch Fix Icon Syntax Errors
# Convert HTML strings in Vue data to pure class names

$ErrorActionPreference = "Stop"

$baseDir = "G:\huiwutong新版合集\huiwutong-uniapp\pages"

# Define file list
$fileList = @(
    "archive\highlights.vue",
    "common\settings.vue",
    "common\profile.vue",
    "common\home.vue",
    "common\help.vue",
    "staff\tenant-detail.vue",
    "staff\task-feedback.vue",
    "staff\mobile-alert.vue",
    "staff\handbook-generate.vue",
    "staff\data-analysis.vue",
    "staff\dashboard.vue",
    "learner\seat-detail.vue",
    "learner\registration-status.vue",
    "learner\highlights.vue",
    "learner\evaluation.vue"
)

Write-Host "Starting batch icon syntax fix..." -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$totalFiles = 0

foreach ($file in $fileList) {
    $filePath = Join-Path $baseDir $file

    if (Test-Path $filePath) {
        $totalFiles++
        Write-Host "Processing: $file" -ForegroundColor Yellow

        try {
            $content = Get-Content -Path $filePath -Encoding UTF8 -Raw
            $originalContent = $content
            $fileChanged = $false

            # Fix HTML strings to pure class names
            $patterns = @(
                @{Old="icon: '<text class=\"fa fa-check\"></text>'";New="icon: 'fa-check'"},
                @{Old="icon: '<text class=\"fa fa-clock\"></text>'";New="icon: 'fa-clock'"},
                @{Old="icon: '<text class=\"fa fa-calendar-alt\"></text>'";New="icon: 'fa-calendar-alt'"},
                @{Old="icon: '<text class=\"fa fa-th-large\"></text>'";New="icon: 'fa-th-large'"},
                @{Old="icon: '<text class=\"fa fa-book\"></text>'";New="icon: 'fa-book'"},
                @{Old="icon: '<text class=\"fa fa-book-open\"></text>'";New="icon: 'fa-book-open'"},
                @{Old="icon: '<text class=\"fa fa-address-book\"></text>'";New="icon: 'fa-address-book'"},
                @{Old="icon: '<text class=\"fa fa-comments\"></text>'";New="icon: 'fa-comments'"},
                @{Old="icon: '<text class=\"fa fa-users\"></text>'";New="icon: 'fa-users'"},
                @{Old="icon: '<text class=\"fa fa-camera\"></text>'";New="icon: 'fa-camera'"},
                @{Old="icon: '<text class=\"fa fa-chart-bar\"></text>'";New="icon: 'fa-chart-bar'"},
                @{Old="icon: '<text class=\"fa fa-chart-line\"></text>'";New="icon: 'fa-chart-line'"},
                @{Old="icon: '<text class=\"fa fa-tasks\"></text>'";New="icon: 'fa-tasks'"},
                @{Old="icon: '<text class=\"fa fa-bullhorn\"></text>'";New="icon: 'fa-bullhorn'"},
                @{Old="icon: '<text class=\"fa fa-microphone\"></text>'";New="icon: 'fa-microphone'"},
                @{Old="icon: '<text class=\"fa fa-edit\"></text>'";New="icon: 'fa-edit'"},
                @{Old="icon: '<text class=\"fa fa-cog\"></text>'";New="icon: 'fa-cog'"},
                @{Old="icon: '<text class=\"fa fa-user\"></text>'";New="icon: 'fa-user'"},
                @{Old="icon: '<text class=\"fa fa-bell\"></text>'";New="icon: 'fa-bell'"},
                @{Old="icon: '<text class=\"fa fa-search\"></text>'";New="icon: 'fa-search'"},
                @{Old="icon: '<text class=\"fa fa-home\"></text>'";New="icon: 'fa-home'"},
                @{Old="icon: '<text class=\"fa fa-qrcode\"></text>'";New="icon: 'fa-qrcode'"},
                @{Old="icon: '<text class=\"fa fa-star\"></text>'";New="icon: 'fa-star'"},
                @{Old="icon: '<text class=\"fa fa-star-o\"></text>'";New="icon: 'fa-star-o'"},
                @{Old="icon: '<text class=\"fa fa-lock\"></text>'";New="icon: 'fa-lock'"},
                @{Old="icon: '<text class=\"fa fa-mobile-alt\"></text>'";New="icon: 'fa-mobile-alt'"},
                @{Old="icon: '<text class=\"fa fa-eye\"></text>'";New="icon: 'fa-eye'"},
                @{Old="icon: '<text class=\"fa fa-eye-slash\"></text>'";New="icon: 'fa-eye-slash'"},
                @{Old="icon: '<text class=\"fa fa-map-marker-alt\"></text>'";New="icon: 'fa-map-marker-alt'"},
                @{Old="icon: '<text class=\"fa fa-file-alt\"></text>'";New="icon: 'fa-file-alt'"},
                @{Old="icon: '<text class=\"fa fa-download\"></text>'";New="icon: 'fa-download'"},
                @{Old="icon: '<text class=\"fa fa-handshake\"></text>'";New="icon: 'fa-handshake'"},
                @{Old="icon: '<text class=\"fa fa-clipboard-check\"></text>'";New="icon: 'fa-clipboard-check'"},
                @{Old="icon: '<text class=\"fa fa-chalkboard-teacher\"></text>'";New="icon: 'fa-chalkboard-teacher'"},
                @{Old="icon: '<text class=\"fa fa-university\"></text>'";New="icon: 'fa-university'"}
            )

            foreach ($p in $patterns) {
                $oldPattern = [regex]::Escape($p.Old)
                if ($content -match $oldPattern) {
                    $content = $content -replace $oldPattern, $p.New
                    $fileChanged = $true
                    $shortOld = $p.Old.Substring(0, [Math]::Min(40, $p.Old.Length)) + "..."
                    Write-Host "  Fixed: $shortOld" -ForegroundColor Green
                }
            }

            if ($fileChanged) {
                $utf8NoBom = New-Object System.Text.UTF8Encoding $false
                [System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
                $fixedCount++
                Write-Host "  [OK] File updated" -ForegroundColor Green
            } else {
                Write-Host "  [SKIP] No changes" -ForegroundColor Gray
            }
        }
        catch {
            Write-Host "  [ERROR] $_" -ForegroundColor Red
        }
    } else {
        Write-Host "[ERROR] File not found: $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Fix Complete!" -ForegroundColor Green
Write-Host "Total files: $totalFiles" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
