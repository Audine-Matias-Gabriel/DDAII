@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
call mvnw.cmd -B test
