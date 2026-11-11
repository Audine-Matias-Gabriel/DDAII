@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
call "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3.5\plugins\maven\lib\maven3\bin\mvn.cmd" -B test
