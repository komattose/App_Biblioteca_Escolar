$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($env:BIBLIOTECA_TEST_CONFIRM -ne 'SI') { throw 'Usa una BD de pruebas y establece BIBLIOTECA_TEST_CONFIRM=SI. Lee PRUEBAS.md.' }
& ./compilar.ps1
$sources = @(Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& javac --release 17 -encoding UTF-8 -cp 'build/classes;lib/*' -d build/test-classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación de pruebas' }
& java -cp 'build/classes;build/test-classes;lib/*' biblioteca.IntegrationTest
if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas' }
