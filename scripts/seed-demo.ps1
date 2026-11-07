#requires -Version 5.1
# Ejecutar SOLO con base local recien migrada (sin datos reales).
param([string]$ServerHost='127.0.0.1',[int]$ServerPort=5432)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$psqlCmd=Get-Command psql.exe -ErrorAction SilentlyContinue
if($psqlCmd){$psql=$psqlCmd.Source}else{$psql='C:\Program Files\PostgreSQL\18\bin\psql.exe'}
if(-not (Test-Path $psql)){throw 'No encontre psql.exe. Agrega PostgreSQL bin al PATH.'}
Write-Host 'ATENCION: los datos demo incluyen cuentas ficticias. Nunca ejecutar en produccion.' -ForegroundColor Yellow
$confirm=Read-Host 'Escribe DEMO para importar los datos ficticios a safelab_db'
if($confirm -cne 'DEMO'){Write-Host 'Cancelado.';exit 0}
& $psql '-X' '-v' 'ON_ERROR_STOP=1' '-h' $ServerHost '-p' "$ServerPort" '-U' 'safelab_migrator' '-d' 'safelab_db' '-f' (Join-Path $root 'database/seed/demo_data.sql')
if($LASTEXITCODE -ne 0){throw 'Error al cargar demo. PostgreSQL revirtio toda la transaccion.'}
Write-Host 'Importacion demo completada. Los registros ya existentes no se borraron.' -ForegroundColor Green
