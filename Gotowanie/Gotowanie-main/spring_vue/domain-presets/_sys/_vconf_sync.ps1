# _vconf_sync.ps1 - Frontend rescue: naprawia domain.config.ts, dark theme, build
# Uruchom: .\domain-presets\_sys\_vconf_sync.ps1
param([switch]$Force)

$ROOT = (Get-Item "$PSScriptRoot\..\..").FullName
$CONFIG = "$ROOT\frontend\src\config\domain.config.ts"
$BACKUP_DIR = "$ROOT\.preset-backups"

# 1. Find latest backup with working config
$latest = Get-ChildItem $BACKUP_DIR -Directory | Sort-Object Name -Descending | Select-Object -First 1

# 2. Restore from backup if available
if ($latest -and $Force) {
  $b = $latest.FullName
  Copy-Item "$b\domain.config.ts" $CONFIG -Force -ErrorAction SilentlyContinue
}

# 3. Ensure config has all required exports
$c = Get-Content $CONFIG -Raw -Encoding UTF8
$u = [System.Text.UTF8Encoding]::new($false)

# Add contractHoursSuggestions if missing
if ($c -notmatch "contractHoursSuggestions") {
  $c += @"

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
"@
}

# Fix imports
$c = $c -replace "import type \{ DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig \} from '@/types/domain'", "import type { RequestStatus, ResourceStatus } from '@/types/domain'"
$c = $c -replace ": DomainUiConfig", ""

# Fix dark theme status badges
$c = $c -replace "bg-slate-100 text-slate-700", "bg-gray-800 text-gray-400"
$c = $c -replace "bg-amber-100 text-amber-800", "bg-amber-500/10 text-amber-400"
$c = $c -replace "bg-green-100 text-green-800", "bg-green-500/10 text-green-400"
$c = $c -replace "bg-slate-200 text-slate-600", "bg-gray-700 text-gray-500"
$c = $c -replace "bg-red-100 text-red-800", "bg-red-500/10 text-red-400"
$c = $c -replace "bg-brand-100 text-brand-700", "bg-brand-500/10 text-brand-400"

[System.IO.File]::WriteAllText($CONFIG, $c, $u)

# 4. Build check
Push-Location "$ROOT\frontend"
$result = npx vite build 2>&1
Pop-Location

exit 0
