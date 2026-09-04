$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root 'backend'
$py = Join-Path $backend '.venv\Scripts\python.exe'
if (-not (Test-Path $py)) { & (Join-Path $root 'setup_cyberquiz.ps1') }
Start-Process powershell -ArgumentList '-NoExit','-Command',"Set-Location '$backend'; & '$py' main.py" | Out-Null
Write-Host 'Backend lancé dans une nouvelle fenêtre.' -ForegroundColor Cyan
Set-Location $root
& .\gradlew.bat :app:assembleDebug
$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw 'APK introuvable après le build.' }
if (Get-Command adb -ErrorAction SilentlyContinue) {
    $devices = adb devices | Select-String '\sdevice$'
    if ($devices) {
        adb install -r $apk
        adb shell am start -n com.example.cyberquiz/.MainActivity
        Write-Host 'CyberQuiz installé et lancé.' -ForegroundColor Green
    } else { Write-Host "APK construit : $apk`nAucun appareil ADB autorisé détecté." -ForegroundColor Yellow }
} else { Write-Host "APK construit : $apk`nADB non trouvé dans le PATH." -ForegroundColor Yellow }
