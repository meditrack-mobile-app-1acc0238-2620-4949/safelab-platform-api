#requires -Version 5.1
<#
 SafeLab v2 - instalacion inicial LOCAL Windows. Requiere PostgreSQL y psql.exe.
 Se ejecuta una sola vez. No elimina bases, tablas ni roles existentes.
 Contraseñas SQL nuevas: psql \password, sin incrustarlas en comandos o archivos.
#>
param(
 [string]$ServerHost = '127.0.0.1',
 [int]$ServerPort = 5432,
 [switch]$ResetPasswords
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$psqlCmd = Get-Command psql.exe -ErrorAction SilentlyContinue
if ($psqlCmd) { $psql = $psqlCmd.Source }
else {
 $candidates = @('C:\Program Files\PostgreSQL\18\bin\psql.exe', 'C:\Program Files\PostgreSQL\17\bin\psql.exe', 'C:\Program Files\PostgreSQL\16\bin\psql.exe')
 $psql = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}
if (-not $psql) { throw 'No encontre psql.exe. Instala PostgreSQL o agrega su carpeta bin al PATH.' }
Write-Host 'SafeLab - configuracion inicial PostgreSQL local' -ForegroundColor Cyan
Write-Host 'Se solicitara la clave LOCAL del administrador postgres. Nunca la subas a Git.'
$secure = Read-Host 'Contrasena de postgres' -AsSecureString
$ptr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $env:PGPASSWORD = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
function Invoke-Psql([string]$database, [string[]]$params) {
 & $psql '-X' '-w' '-v' 'ON_ERROR_STOP=1' '-h' $ServerHost '-p' "$ServerPort" '-U' 'postgres' '-d' $database @params
 if ($LASTEXITCODE -ne 0) { throw "Error de PostgreSQL (psql, codigo $LASTEXITCODE). No se borro ningun dato." }
}
try {
 $oldMigratorHasPassword = (& $psql '-X' '-w' '-t' '-A' '-h' $ServerHost '-p' "$ServerPort" '-U' 'postgres' '-d' 'postgres' '-c' "SELECT 1 FROM pg_authid WHERE rolname='safelab_migrator' AND rolpassword IS NOT NULL" ).Trim() -eq '1'
 $oldAppHasPassword = (& $psql '-X' '-w' '-t' '-A' '-h' $ServerHost '-p' "$ServerPort" '-U' 'postgres' '-d' 'postgres' '-c' "SELECT 1 FROM pg_authid WHERE rolname='safelab_app' AND rolpassword IS NOT NULL" ).Trim() -eq '1'
 Invoke-Psql 'postgres' @('-f', (Join-Path $root 'database/setup/01_roles_and_db.sql'))
 Invoke-Psql 'safelab_db' @('-f', (Join-Path $root 'database/setup/02_database_grants.sql'))
 if (-not $oldMigratorHasPassword -or $ResetPasswords) {
   Write-Host "Establece ahora la NUEVA contrasena de safelab_migrator (aparecera dos veces):" -ForegroundColor Cyan
   Invoke-Psql 'postgres' @('-c', '\password safelab_migrator')
 }
 if (-not $oldAppHasPassword -or $ResetPasswords) {
   Write-Host "Establece ahora la NUEVA contrasena de safelab_app (aparecera dos veces):" -ForegroundColor Cyan
   Invoke-Psql 'postgres' @('-c', '\password safelab_app')
 }
 Write-Host 'Configuracion terminada. En IntelliJ agrega DB_PASSWORD y FLYWAY_PASSWORD (distintas).' -ForegroundColor Green
 Write-Host 'En cuentas preexistentes NO se cambiaron las contrasenas. Para ello usa -ResetPasswords.'
} finally { Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue }
