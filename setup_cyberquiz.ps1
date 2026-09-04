$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root 'backend'
Write-Host '=== CyberQuiz : préparation ===' -ForegroundColor Cyan
if (-not (Get-Command python -ErrorAction SilentlyContinue)) { throw 'Python est requis. Installe Python 3.11+ puis relance.' }
if (-not (Test-Path (Join-Path $backend '.venv'))) { python -m venv (Join-Path $backend '.venv') }
$py = Join-Path $backend '.venv\Scripts\python.exe'
& $py -m pip install --upgrade pip
& $py -m pip install -r (Join-Path $backend 'requirements.txt')
if (-not (Test-Path (Join-Path $backend '.env'))) { Copy-Item (Join-Path $backend '.env.example') (Join-Path $backend '.env') }
Write-Host 'Préparation terminée. Renseigne OPENAI_API_KEY dans backend\.env pour activer les questions IA.' -ForegroundColor Green
