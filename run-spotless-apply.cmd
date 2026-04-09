@echo off
setlocal

call mvnw.cmd spotless:apply
exit /b %errorlevel%
