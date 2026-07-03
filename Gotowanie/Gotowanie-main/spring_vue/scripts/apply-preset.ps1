# apply-preset.ps1 - Switch domain preset
# Usage: .\scripts\apply-preset.ps1 <preset-name>
#        .\scripts\apply-preset.ps1 -List
param(
  [Parameter(Position = 0)]
  [string]$PresetName = "",
  [switch]$List
)

$ROOT = Split-Path -Parent $PSScriptRoot
$PRESETS_DIR = Join-Path $ROOT "domain-presets"
$BACKEND_DIR = Join-Path $ROOT "backend\src\main\java\pl\zpo\app"
$FRONTEND_DIR = Join-Path $ROOT "frontend\src\config"

# ----- LIST MODE -----
if ($List -or $PresetName -eq "" -or $PresetName -eq "-List") {
  Write-Host "`n=== DOSTEPNE PRESETY (20) ===`n" -ForegroundColor Cyan
  $presets = Get-ChildItem -LiteralPath $PRESETS_DIR -Directory | Sort-Object Name
  
  $i = 0
  foreach ($p in $presets) {
    $readme = Join-Path $p.FullName "README_PRESET.md"
    $topic = ""
    if (Test-Path $readme) {
      $content = Get-Content $readme | Out-String
      if ($content -match "Temat:\s*\*\*(.+?)\*\*") { $topic = $Matches[1] }
      elseif ($content -match "Temat:\s*(.+?)\n") { $topic = $Matches[1].Trim() }
    }
    $i++
    Write-Host "  $($i.ToString().PadLeft(2)). " -NoNewline -ForegroundColor DarkGray
    Write-Host $p.Name.PadRight(28) -NoNewline -ForegroundColor Yellow
    Write-Host "| $topic" -ForegroundColor White
  }
  
  Write-Host "`nUzycie: .\scripts\apply-preset.ps1 <nazwa-presetu>" -ForegroundColor Gray
  Write-Host "Np:     .\scripts\apply-preset.ps1 conference-room-booking`n" -ForegroundColor Gray
  return
}

# ----- VALIDATE -----
$PRESET_DIR = Join-Path $PRESETS_DIR $PresetName
if (-not (Test-Path $PRESET_DIR)) {
  Write-Host "Brak presetu: $PresetName" -ForegroundColor Red
  Write-Host "Uzyj -List zeby zobaczyc dostepne." -ForegroundColor Yellow
  exit 1
}

$SRC_BACKEND = Join-Path $PRESET_DIR "backend"
$SRC_FRONTEND = Join-Path $PRESET_DIR "frontend"

$FILES = @{
  "$SRC_BACKEND\DomainProfileProvider.java" = "$BACKEND_DIR\domain\config\DomainProfileProvider.java"
  "$SRC_BACKEND\DataInitializer.java"       = "$BACKEND_DIR\config\DataInitializer.java"
  "$SRC_BACKEND\DefaultDomainAlgorithm.java" = "$BACKEND_DIR\domain\algorithm\DefaultDomainAlgorithm.java"
  "$SRC_FRONTEND\domain.config.ts"          = "$FRONTEND_DIR\domain.config.ts"
}

foreach ($src in $FILES.Keys) {
  if (-not (Test-Path $src)) {
    Write-Host "Brak pliku w presecie: $src" -ForegroundColor Red
    exit 1
  }
}

# ----- BACKUP -----
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$BACKUP_DIR = Join-Path $ROOT ".preset-backups\$TIMESTAMP"
New-Item -ItemType Directory -Force -Path "$BACKUP_DIR\backend" -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Force -Path "$BACKUP_DIR\frontend" -ErrorAction SilentlyContinue | Out-Null

Write-Host "`n=== BACKUP ===   " -NoNewline -ForegroundColor Cyan
foreach ($dest in $FILES.Values) {
  if (Test-Path $dest) {
    $fname = Split-Path -Leaf $dest
    Copy-Item $dest "$BACKUP_DIR\$fname" -Force
  }
}
Write-Host "$BACKUP_DIR" -ForegroundColor Gray

# ----- APPLY -----
Write-Host "`n=== KOPIUJE PLIKI PRESETU ===`n" -ForegroundColor Cyan
foreach ($src in $FILES.Keys) {
  $dest = $FILES[$src]
  Copy-Item $src $dest -Force
  (Get-Item $dest).LastWriteTime = Get-Date
  $relDest = $dest.Replace($ROOT, "").TrimStart("\")
  Write-Host "  OK  " -NoNewline -ForegroundColor Green
  Write-Host $relDest -ForegroundColor Gray
}

# ----- PATCH domain.config.ts with missing exports -----
$configFile = "$FRONTEND_DIR\domain.config.ts"
$configContent = Get-Content $configFile -Raw -Encoding UTF8

# Fix imports: keep only needed types, remove DomainFieldConfig if unused
$configContent = $configContent -replace "import type \{ DomainFieldConfig, RequestStatus, ResourceStatus(, DomainUiConfig)? \} from '@/types/domain'", "import type { RequestStatus, ResourceStatus } from '@/types/domain'"
$configContent = $configContent -replace "import type \{ DomainFieldConfig \} from '@/types/domain'", ""
$configContent = $configContent -replace "import type \{ DomainFieldConfig, RequestStatus, ResourceStatus \} from '@/types/domain'", "import type { RequestStatus, ResourceStatus } from '@/types/domain'"
$configContent = $configContent -replace "import type \{ RequestStatus, ResourceStatus, DomainFieldConfig \} from '@/types/domain'", "import type { RequestStatus, ResourceStatus } from '@/types/domain'"
$configContent = $configContent -replace ": DomainUiConfig", ""

# Only add shifts for scheduler-type presets (those with shiftType in request fields)
$isShiftPreset = $configContent -match "shiftType"
if ($isShiftPreset -and $configContent -notmatch "shifts:") {
  $shiftsAdd = @"

  shifts: {
    morning: { label: 'Zmiana poranna', start: '07:00', end: '15:00' },
    evening: { label: 'Zmiana wieczorna', start: '15:00', end: '23:00' },
  },
"@
  $configContent = $configContent -replace "(labels:\s*\{)", "$shiftsAdd`n  `$1"
}

# Add missing labels (calendar etc) if missing
$labelKeys = @('calendar', 'delete:', 'occupied:', 'free:', 'morning:', 'evening:')
$needsLabels = $false
foreach ($k in $labelKeys) {
  if ($configContent -notmatch $k) { $needsLabels = $true; break }
}
if ($needsLabels) {
  $missingLabels = @"

    calendar: 'Kalendarz',
    delete: 'Usuń',
    occupied: 'Zajęte',
    free: 'Wolne',
    morning: 'Zmiana poranna',
    evening: 'Zmiana wieczorna',
"@
  $configContent = $configContent -replace "(availableOnly:.*?',)", "`$1`n$missingLabels"
}

# Fix dark theme status badge classes
$configContent = $configContent -replace "bg-slate-100 text-slate-700", "bg-gray-800 text-gray-400"
$configContent = $configContent -replace "bg-amber-100 text-amber-800", "bg-amber-500/10 text-amber-400"
$configContent = $configContent -replace "bg-green-100 text-green-800", "bg-green-500/10 text-green-400"
$configContent = $configContent -replace "bg-slate-200 text-slate-600", "bg-gray-700 text-gray-500"
$configContent = $configContent -replace "bg-red-100 text-red-800", "bg-red-500/10 text-red-400"
$configContent = $configContent -replace "bg-brand-100 text-brand-700", "bg-brand-500/10 text-brand-400"

# Add contractHoursSuggestions if missing
if ($configContent -notmatch "contractHoursSuggestions") {
  $contractAdd = @"

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
"@
  $configContent += "`n$contractAdd"
}

try {
  $utf8 = [System.Text.UTF8Encoding]::new($false)
  [System.IO.File]::WriteAllText($configFile, $configContent, $utf8)
  Write-Host "`n  PATCH " -NoNewline -ForegroundColor Yellow
  Write-Host "dodano brakujace eksporty do domain.config.ts" -ForegroundColor Gray
} catch {
  Write-Host "`n  PATCH " -NoNewline -ForegroundColor Yellow
  Write-Host "nie udalo sie (zamknij Vite dev server) - plik zostal skopiowany bez patcha" -ForegroundColor DarkYellow
}

# ----- SHOW WHAT CHANGED -----
Write-Host "`n=== CO ZMIENIONO ===`n" -ForegroundColor Magenta

$newConfig = Join-Path $FRONTEND_DIR "domain.config.ts"
if (Test-Path $newConfig) {
  $config = Get-Content $newConfig -Raw
  if ($config -match "appName:\s*'(.+?)'") { 
    Write-Host "  Temat:     " -NoNewline -ForegroundColor Gray
    Write-Host $Matches[1] -ForegroundColor White 
  }
  if ($config -match "singular:\s*'(.+?)'") { 
    Write-Host "  Zasob:     " -NoNewline -ForegroundColor Gray
    Write-Host $Matches[1] -ForegroundColor White 
  }
  if ($config -match "request:.*?singular:\s*'(.+?)'" -or $config -match "request:[\s\S]*?singular:\s*'(.+?)'") {
    Write-Host "  Request:   " -NoNewline -ForegroundColor Gray
    Write-Host $Matches[1] -ForegroundColor White 
  }
  if ($config -match "timeMode:\s*'(.+?)'") { 
    Write-Host "  TimeMode:  " -NoNewline -ForegroundColor Gray
    Write-Host $Matches[1] -ForegroundColor White 
  }
}

$readmePath = Join-Path $PRESET_DIR "README_PRESET.md"
if (Test-Path $readmePath) {
  Write-Host "`n  Szczegoly: " -NoNewline -ForegroundColor Gray
  Write-Host "$readmePath" -ForegroundColor DarkGray
}

# ----- NEXT STEPS -----
Write-Host "`n=== CO DALEJ ===`n" -ForegroundColor Cyan
  Write-Host "  1. Zrestartuj backend:" -ForegroundColor Yellow
  Write-Host "     cd backend; ./mvnw spring-boot:run" -ForegroundColor White
  Write-Host "     (DataInitializer automatycznie wyczysci stare dane)" -ForegroundColor Gray
Write-Host "  2. Frontend: cd frontend; npm run dev" -ForegroundColor Yellow
Write-Host "  3. Testy:   cd backend; ./mvnw test" -ForegroundColor Yellow
Write-Host "`n  Cofnij:  .\scripts\apply-preset.ps1 employee-scheduler" -ForegroundColor DarkGray
Write-Host "  (backup w: $BACKUP_DIR)`n" -ForegroundColor DarkGray
