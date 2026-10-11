$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build/classes | Out-Null
$javaHomeLine = (& java -XshowSettings:properties -version 2>&1 | Out-String)
if ($javaHomeLine -match 'java.home\s*=\s*([^\r\n]+)') { $jdkBin = Join-Path $Matches[1].Trim() 'bin' } else { throw 'No se encontró el JDK. Instala Java 17 o superior.' }
$sources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& (Join-Path $jdkBin 'javac.exe') --release 17 -encoding UTF-8 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación' }
& (Join-Path $jdkBin 'jar.exe') --create --file BibliotecaEscolar.jar --manifest manifest.mf -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'Falló la creación del JAR' }
Write-Host 'Compilación correcta. Ejecuta iniciar.bat.'
