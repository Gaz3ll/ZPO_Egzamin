# _ptopic_adapt.ps1 - Adaptacja presetu po zmianach
# Uruchom: .\domain-presets\_sys\_ptopic_adapt.ps1 <preset-name>
param([string]$PresetName = "")

if ($PresetName -eq "") {
  # No output - silent mode
  exit 0
}

$ROOT = (Get-Item "$PSScriptRoot\..\..").FullName
$PRESET_DIR = "$ROOT\domain-presets\$PresetName"
$BKDIR = "$ROOT\backend\src\main\java\pl\zpo\app"

if (-not (Test-Path $PRESET_DIR)) { exit 1 }

$u = [System.Text.UTF8Encoding]::new($false)

# 1. Fix backend - remove BOM from preset
Get-ChildItem "$PRESET_DIR\backend" -Recurse -Filter *.java | ForEach-Object {
  $b = [System.IO.File]::ReadAllBytes($_.FullName)
  if ($b.Length -ge 3 -and $b[0] -eq 0xEF) {
    [System.IO.File]::WriteAllBytes($_.FullName, $b[3..($b.Length-1)])
  }
}

# 2. Fix preset DomainProfileProvider AlgorithmMode
$profileFile = "$PRESET_DIR\backend\DomainProfileProvider.java"
if (Test-Path $profileFile) {
  $c = Get-Content $profileFile -Raw
  $c = $c -replace "AlgorithmMode\.(?!VALUE_CALCULATION_ONLY|TIME_AVAILABILITY_AND_CALCULATION)\w+", "AlgorithmMode.VALUE_CALCULATION_ONLY"
  [System.IO.File]::WriteAllText($profileFile, $c, $u)
}

# 3. Fix preset domain.config.ts
$configFile = "$PRESET_DIR\frontend\domain.config.ts"
if (Test-Path $configFile) {
  $c = Get-Content $configFile -Raw
  $c = $c -replace "bg-slate-100 text-slate-700", "bg-gray-800 text-gray-400"
  $c = $c -replace "bg-amber-100 text-amber-800", "bg-amber-500/10 text-amber-400"
  $c = $c -replace "bg-green-100 text-green-800", "bg-green-500/10 text-green-400"
  $c = $c -replace "bg-slate-200 text-slate-600", "bg-gray-700 text-gray-500"
  $c = $c -replace "bg-red-100 text-red-800", "bg-red-500/10 text-red-400"
  $c = $c -replace "bg-brand-100 text-brand-700", "bg-brand-500/10 text-brand-400"
  
  if ($c -notmatch "contractHoursSuggestions") {
    $c += @"

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
"@
  }
  [System.IO.File]::WriteAllText($configFile, $c, $u)
}

# 4. Apply preset to active location
Copy-Item "$profileFile" "$BKDIR\domain\config\DomainProfileProvider.java" -Force
Copy-Item "$PRESET_DIR\backend\DataInitializer.java" "$BKDIR\config\DataInitializer.java" -Force
Copy-Item "$PRESET_DIR\backend\DefaultDomainAlgorithm.java" "$BKDIR\domain\algorithm\DefaultDomainAlgorithm.java" -Force
Copy-Item "$configFile" "$ROOT\frontend\src\config\domain.config.ts" -Force

# 5. Touch files to trigger Maven recompile
Get-Item "$BKDIR\domain\config\DomainProfileProvider.java" | ForEach-Object { $_.LastWriteTime = Get-Date }
Get-Item "$BKDIR\config\DataInitializer.java" | ForEach-Object { $_.LastWriteTime = Get-Date }
Get-Item "$BKDIR\domain\algorithm\DefaultDomainAlgorithm.java" | ForEach-Object { $_.LastWriteTime = Get-Date }

exit 0
