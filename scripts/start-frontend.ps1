param(
  [string]$ApiTarget = 'http://127.0.0.1:8080',
  [int]$Port = 5173
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $projectRoot 'frontend'
Push-Location $frontendRoot
try {
  if (-not (Test-Path -LiteralPath (Join-Path $frontendRoot 'node_modules'))) { npm install }
  $env:VITE_API_TARGET = $ApiTarget
  npm run dev -- --port $Port
}
finally { Remove-Item Env:VITE_API_TARGET -ErrorAction SilentlyContinue; Pop-Location }
