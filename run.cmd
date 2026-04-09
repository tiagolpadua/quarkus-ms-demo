@echo off
setlocal

call mvnw.cmd quarkus:dev
exit /b %errorlevel%
