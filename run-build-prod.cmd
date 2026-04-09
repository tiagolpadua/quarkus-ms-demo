@echo off
setlocal

call mvnw.cmd clean verify %*
exit /b %errorlevel%
