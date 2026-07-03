# _exam_apply.ps1 - Auto-apply exam topic from file
# Usage: .\domain-presets\_sys\_exam_apply.ps1 "C:\ścieżka\plik.txt"
# The .txt file should contain the exam topic name (e.g. "Ocena filmów")

param([string]$TopicFile = "")

if ($TopicFile -eq "" -or -not (Test-Path $TopicFile)) {
  # Silent fail - no output
  exit 1
}

$topic = (Get-Content $TopicFile -Raw).Trim()
$ROOT = (Get-Item "$PSScriptRoot\..\..").FullName
$u = [System.Text.UTF8Encoding]::new($false)

# Topic → preset mapping
$map = @{
  "Ocena filmów" = "movie-ratings"
  "Ocena filmow" = "movie-ratings"
  "Sale konferencyjne" = "conference-room-booking"
  "Rezerwacja sal" = "conference-room-booking"
  "Zajecia fitness" = "fitness-waitlist"
  "Fitness" = "fitness-waitlist"
  "Listy rezerwowe" = "fitness-waitlist"
  "Grafik pracowników" = "employee-scheduler"
  "Grafik pracownikow" = "employee-scheduler"
  "Zarzadzanie pracownikami" = "employee-scheduler"
  "Employee scheduler" = "employee-scheduler"
  "Catering dietetyczny" = "meal-planning-catering"
  "Planowanie posilkow" = "meal-planning-catering"
  "Schronisko" = "animal-shelter"
  "Schronisko zwierzat" = "animal-shelter"
  "Platforma e-learningowa" = "quiz-platform"
  "Quizy" = "quiz-platform"
  "Ocenianie quizow" = "quiz-platform"
  "Kursy online" = "online-course-platform"
  "Platforma kursow" = "online-course-platform"
  "Ranking graczy" = "player-ranking"
  "Ranking" = "player-ranking"
  "Esport" = "player-ranking"
  "Habit tracker" = "habit-tracker"
  "Nawyki" = "habit-tracker"
  "Biblioteka" = "library-book-rental"
  "Wypozyczanie ksiazek" = "library-book-rental"
  "Zgloszenia administracji" = "estate-issue-reporting"
  "Zgloszenia osiedlowe" = "estate-issue-reporting"
  "Administracja osiedla" = "estate-issue-reporting"
  "Rejestr treningow" = "workout-tracker"
  "Silownia" = "workout-tracker"
  "Treningi" = "workout-tracker"
  "Plan nauki" = "exam-study-planner"
  "Egzaminy" = "exam-study-planner"
  "Adopcja roslin" = "office-plant-care"
  "Rosliny" = "office-plant-care"
  "Dziennik nastroju" = "mood-diary"
  "Nastroj" = "mood-diary"
  "Sklep" = "shop"
  "Sklep internetowy" = "shop"
}

$presetName = $null
foreach ($key in $map.Keys) {
  if ($topic -match $key) { $presetName = $map[$key]; break }
}

if (-not $presetName) {
  # Try direct preset name match
  if (Test-Path "$ROOT\domain-presets\$topic") { $presetName = $topic }
  else { exit 2 }
}

$PRESET_DIR = "$ROOT\domain-presets\$presetName"
if (-not (Test-Path $PRESET_DIR)) { exit 3 }

# --- Apply preset files ---
$BKDIR = "$ROOT\backend\src\main\java\pl\zpo\app"
$FEDIR = "$ROOT\frontend\src\config"

Copy-Item "$PRESET_DIR\backend\DomainProfileProvider.java" "$BKDIR\domain\config\DomainProfileProvider.java" -Force
Copy-Item "$PRESET_DIR\backend\DataInitializer.java" "$BKDIR\config\DataInitializer.java" -Force
Copy-Item "$PRESET_DIR\backend\DefaultDomainAlgorithm.java" "$BKDIR\domain\algorithm\DefaultDomainAlgorithm.java" -Force
Copy-Item "$PRESET_DIR\frontend\domain.config.ts" "$FEDIR\domain.config.ts" -Force

# Remove BOM from backend files
Get-ChildItem $BKDIR -Recurse -Filter *.java | ForEach-Object {
  $b = [System.IO.File]::ReadAllBytes($_.FullName)
  if ($b.Length -ge 3 -and $b[0] -eq 0xEF) { [System.IO.File]::WriteAllBytes($_.FullName, $b[3..($b.Length-1)]) }
}

# Fix AlgorithmMode in all presets
Get-ChildItem "$ROOT\domain-presets" -Recurse -Filter "DomainProfileProvider.java" | ForEach-Object {
  $c = Get-Content $_.FullName -Raw
  if ($c -match "AlgorithmMode\.(?!VALUE_CALCULATION_ONLY|TIME_AVAILABILITY_AND_CALCULATION)\w+") {
    $c = $c -replace "AlgorithmMode\.\w+", "AlgorithmMode.VALUE_CALCULATION_ONLY"
    [System.IO.File]::WriteAllText($_.FullName, $c, $u)
  }
}

# Patch domain.config.ts
$configFile = "$FEDIR\domain.config.ts"
$cc = Get-Content $configFile -Raw

# Remove old imports, add needed ones
$cc = $cc -replace "import type.*from '@/types/domain'", ""
$cc = $cc -replace ": DomainUiConfig", ""

# Fix dark theme classes
$cc = $cc -replace "bg-slate-100 text-slate-700", "bg-gray-800 text-gray-400"
$cc = $cc -replace "bg-amber-100 text-amber-800", "bg-amber-500/10 text-amber-400"
$cc = $cc -replace "bg-green-100 text-green-800", "bg-green-500/10 text-green-400"
$cc = $cc -replace "bg-slate-200 text-slate-600", "bg-gray-700 text-gray-500"
$cc = $cc -replace "bg-red-100 text-red-800", "bg-red-500/10 text-red-400"
$cc = $cc -replace "bg-brand-100 text-brand-700", "bg-brand-500/10 text-brand-400"

# Add shifts for scheduler presets
if ($cc -match "shiftType" -and $cc -notmatch "shifts:") {
  $shiftAdd = @"

  shifts: {
    morning: { label: 'Zmiana poranna', start: '07:00', end: '15:00' },
    evening: { label: 'Zmiana wieczorna', start: '15:00', end: '23:00' },
  },
"@
  $cc = $cc -replace "(labels:\s*\{)", "$shiftAdd`n  `$1"
}

# Add contractHoursSuggestions
if ($cc -notmatch "contractHoursSuggestions") {
  $cc += @"

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
"@
}

[System.IO.File]::WriteAllText($configFile, $cc, $u)

# Touch files to force Maven recompile
Get-Item "$BKDIR\domain\config\DomainProfileProvider.java" | ForEach-Object { $_.LastWriteTime = Get-Date }
Get-Item "$BKDIR\config\DataInitializer.java" | ForEach-Object { $_.LastWriteTime = Get-Date }
Get-Item "$BKDIR\domain\algorithm\DefaultDomainAlgorithm.java" | ForEach-Object { $_.LastWriteTime = Get-Date }

exit 0
