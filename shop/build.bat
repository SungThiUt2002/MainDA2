@echo off
setlocal enabledelayedexpansion

REM Build script for frontend/shop Docker image
REM Usage:
REM   build.bat                -> builds with tag 'latest'
REM   build.bat v1.0.0         -> builds with tag 'v1.0.0'
REM   build.bat --no-cache     -> builds latest with no cache
REM   build.bat v1.0.0 --no-cache -> builds v1.0.0 with no cache

set IMAGE_NAME=shop-frontend
set TAG=latest
set NO_CACHE=

for %%A in (%*) do (
  if /I "%%~A"=="--no-cache" set NO_CACHE=--no-cache
)

if not "%1"=="" (
  if /I not "%1"=="--no-cache" set TAG=%1
)

echo.
echo Building %IMAGE_NAME%:%TAG% ...
echo Context: %~dp0
echo.

pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
  echo Failed to change directory to script location.
  exit /b 1
)

docker build %NO_CACHE% -t %IMAGE_NAME%:%TAG% -f Dockerfile .
if errorlevel 1 (
  echo Docker build failed.
  popd >nul 2>&1
  exit /b 1
)

echo.
echo Successfully built %IMAGE_NAME%:%TAG%
popd >nul 2>&1
exit /b 0


