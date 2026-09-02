param(
  [string]$DbUsername = 'root',
  [string]$DbUrl = 'jdbc:mysql://127.0.0.1:3306/catmate?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false',
  [int]$Port = 8080,
  [string]$MavenPath = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$backendRoot = Join-Path $projectRoot 'backend'
if (-not $MavenPath) {
  $mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
  if ($mavenCommand) { $MavenPath = $mavenCommand.Source }
  else {
    $localMaven = 'D:\program\OPC · CODEX · 课设\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd'
    if (Test-Path -LiteralPath $localMaven) { $MavenPath = $localMaven }
  }
}
if (-not $MavenPath -or -not (Test-Path -LiteralPath $MavenPath)) { throw '未找到 Maven。请通过 -MavenPath 指定 bin\mvn.cmd。' }
$securePassword = Read-Host '请输入 MySQL 密码（输入内容不会显示）' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
  $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
  $env:DB_USERNAME = $DbUsername; $env:DB_URL = $DbUrl; $env:SERVER_PORT = "$Port"
  Push-Location $backendRoot
  try { & $MavenPath spring-boot:run } finally { Pop-Location }
}
finally {
  [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
  Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}
