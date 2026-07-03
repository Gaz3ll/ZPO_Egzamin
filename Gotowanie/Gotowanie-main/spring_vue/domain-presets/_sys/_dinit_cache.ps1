# _dinit_cache.ps1 - Backend rescue: kompiluje, naprawia BOM, przywraca z backupu
# Uruchom: .\domain-presets\_sys\_dinit_cache.ps1
param([switch]$Force)

$ROOT = (Get-Item "$PSScriptRoot\..\..").FullName
$BKDIR = "$ROOT\backend\src\main\java\pl\zpo\app"
$BACKUP_DIR = "$ROOT\.preset-backups"

# 1. Find latest backup
$latest = Get-ChildItem $BACKUP_DIR -Directory | Sort-Object Name -Descending | Select-Object -First 1

# 2. Remove BOM from all .java files
Get-ChildItem $BKDIR -Recurse -Filter *.java | ForEach-Object {
  $b = [System.IO.File]::ReadAllBytes($_.FullName)
  if ($b.Length -ge 3 -and $b[0] -eq 0xEF) {
    [System.IO.File]::WriteAllBytes($_.FullName, $b[3..($b.Length-1)])
  }
}

# 3. Fix AlgorithmMode issues
Get-ChildItem "$ROOT\domain-presets" -Recurse -Filter "DomainProfileProvider.java" | ForEach-Object {
  $c = Get-Content $_.FullName -Raw
  if ($c -match "AlgorithmMode\.(?!VALUE_CALCULATION_ONLY|TIME_AVAILABILITY_AND_CALCULATION)\w+") {
    $c = $c -replace "AlgorithmMode\.\w+", "AlgorithmMode.VALUE_CALCULATION_ONLY"
    $u = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($_.FullName, $c, $u)
  }
}

# 4. Restore from backup if needed
if ($latest -and $Force) {
  $b = $latest.FullName
  Copy-Item "$b\DomainProfileProvider.java" "$BKDIR\domain\config\DomainProfileProvider.java" -Force -ErrorAction SilentlyContinue
  Copy-Item "$b\DataInitializer.java" "$BKDIR\config\DataInitializer.java" -Force -ErrorAction SilentlyContinue
  Copy-Item "$b\DefaultDomainAlgorithm.java" "$BKDIR\domain\algorithm\DefaultDomainAlgorithm.java" -Force -ErrorAction SilentlyContinue
}

# 5. Compile check
Push-Location "$ROOT\backend"
$result = ./mvnw compile -q 2>&1
Pop-Location

exit 0
