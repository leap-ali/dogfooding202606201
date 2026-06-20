@echo off
cd /d %~dp0
echo Start npm install at %date% %time% > install.log
"D:\Program Files\nodejs\npm.cmd" install --no-audit --no-fund >> install.log 2>&1
echo npm install exit code: %errorlevel% >> install.log
echo End at %date% %time% >> install.log
exit /b %errorlevel%
