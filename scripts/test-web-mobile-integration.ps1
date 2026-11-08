param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [string]$Identifier = "gerente.andes@safelab.test",
    [switch]$DeleteCreatedAsset
)

$ErrorActionPreference = "Stop"
Write-Host "SafeLab: prueba HTTP contra $BaseUrl"
$secret = Read-Host -AsSecureString "Contrasena de la cuenta de prueba (no SQL)"
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
$login = Invoke-RestMethod -Method POST -Uri "$BaseUrl/auth/login" -ContentType 'application/json' -Body (@{identifier=$Identifier;password=$password}|ConvertTo-Json)
Remove-Variable password
if (-not $login.accessToken) { throw "Login sin accessToken" }
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
$snapshot = Invoke-RestMethod -Method GET -Uri "$BaseUrl/integration/bootstrap" -Headers $headers
if (-not $snapshot.currentUser) { throw 'Falta currentUser' }
if ($snapshot.currentUser.password) { throw 'Contraseña expuesta por bootstrap' }
Write-Host "Usuario: $($snapshot.currentUser.email); assets=$(@($snapshot.assets).Count); sensors=$(@($snapshot.sensors).Count)"
$facility = @($snapshot.facilities)[0]
if (-not $facility) { throw 'La cuenta no tiene sedes para el POST de prueba' }
$assetName = "QA Web-Mobile " + [Guid]::NewGuid().ToString().Substring(0,8)
$body = @{ name=$assetName; category='Cold Storage'; facilityId=$facility.id; storageUnit='QA'; location='Lab QA'; responsible='Smoke test' } | ConvertTo-Json
$created = Invoke-RestMethod -Method POST -Uri "$BaseUrl/facilityassets/assets" -Headers $headers -ContentType 'application/json' -Body $body
if (-not $created.id) { throw 'POST sin id' }
Write-Host "POST: $($created.id)"
$patch = Invoke-RestMethod -Method PATCH -Uri "$BaseUrl/facilityassets/assets/$($created.id)" -Headers $headers -ContentType 'application/json' -Body '{"location":"QA - Modified"}'
if ($patch.location -ne 'QA - Modified') { throw 'PATCH no devolvió location actualizada' }
$get = Invoke-RestMethod -Method GET -Uri "$BaseUrl/facilityassets/assets/$($created.id)" -Headers $headers
if ($get.location -ne 'QA - Modified') { throw 'GET no persistió PATCH' }
Write-Host "OK POST/PATCH/GET: $($created.id)"
if ($DeleteCreatedAsset) {
    Invoke-RestMethod -Method DELETE -Uri "$BaseUrl/facilityassets/assets/$($created.id)" -Headers $headers | Out-Null
    Write-Host 'Asset de QA eliminado.'
} else { Write-Host 'Asset QA conservado para verificar desde Web/Mobile y pgAdmin.' }
