@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
"%JAVA_HOME%\bin\java.exe" -cp "%SCRIPT_DIR%..\target\classes" com.hashbreaker.Seance4ComparaisonBinaire
