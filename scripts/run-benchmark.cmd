@echo off
REM Lance le jar HashBreaker (target/hashbreaker.jar) - a utiliser avec hyperfine :
REM   hyperfine --warmup 1 --runs 5 scripts\run-benchmark.cmd
setlocal
set "SCRIPT_DIR=%~dp0"
"%JAVA_HOME%\bin\java.exe" -jar "%SCRIPT_DIR%..\target\hashbreaker.jar"
