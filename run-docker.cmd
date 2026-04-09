@echo off
setlocal

if "%IMAGE_NAME%"=="" set "IMAGE_NAME=quarkus-ms-demo:jvm"
if "%CONTAINER_NAME%"=="" set "CONTAINER_NAME=quarkus-ms-demo"
if "%HOST_PORT%"=="" set "HOST_PORT=8080"

if not exist target\quarkus-app (
  echo Artefatos de producao nao encontrados em target\quarkus-app.
  echo Executando build de producao antes de iniciar o container...
  call run-build-prod.cmd
  if errorlevel 1 exit /b %errorlevel%
)

docker build -f src/main/docker/Dockerfile.jvm -t "%IMAGE_NAME%" .
if errorlevel 1 exit /b %errorlevel%

docker run --rm --name "%CONTAINER_NAME%" -p %HOST_PORT%:8080 "%IMAGE_NAME%"
exit /b %errorlevel%
