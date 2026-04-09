@echo off
setlocal

call mvnw.cmd test spotless:check
exit /b %errorlevel%
