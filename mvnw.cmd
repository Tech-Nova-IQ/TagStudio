@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0%"=="" (
  SET "__MVNW_ARG0=%~f0"
)
@IF "%MVNW_VERBOSE%" == "true" (
  @echo on
) else (
  @echo off
)

SETLOCAL enableextensions enabledelayedexpansion

set ERROR_CODE=0

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist %WRAPPER_JAR% goto run

call :downloadJar %WRAPPER_JAR%
if exist %WRAPPER_JAR% goto run

echo Could not find %WRAPPER_JAR%, please install maven or download wrapper jar.
goto error

:run
set JVM_CONFIG="%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config"
if exist %JVM_CONFIG% (
    for /F "usebackq delims=" %%a in (%JVM_CONFIG%) do set MAVEN_OPTS=!MAVEN_OPTS! %%a
)

set MAVEN_CMD_LINE_ARGS=%*

if exist "%MAVEN_PROJECTBASEDIR%\.mvn\maven.config" (
    for /F "usebackq delims=" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\maven.config") do set MAVEN_CMD_LINE_ARGS=%%a !MAVEN_CMD_LINE_ARGS!
)

java "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:downloadJar
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%WRAPPER_JAR:\=/%')"
goto :eof

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
