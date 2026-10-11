@echo off
cd /d "%~dp0"
java -jar BibliotecaEscolar.jar
if errorlevel 1 pause
