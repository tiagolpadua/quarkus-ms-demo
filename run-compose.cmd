@echo off
setlocal

echo Gerando artefatos de producao...
call run-build-prod.cmd
if errorlevel 1 exit /b %errorlevel%

echo Subindo stack com docker compose...
docker compose up --build %*
exit /b %errorlevel%
