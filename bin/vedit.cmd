@echo off
REM Copyright (c) 2010 Guidewire Software, Inc.

setLocal EnableDelayedExpansion

SET _BIN_DIR=%~dp0
SET _LIB_DIR=%_BIN_DIR%\..\lib
SET _DEBUG=
SET _CMD_LINE_ARGS=

:checkJava
SET _JAVACMD=%JAVACMD%

IF "%JAVA_HOME%" == "" GOTO noJavaHome
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" GOTO noJavaHome
IF "%_JAVACMD%" == "" SET _JAVACMD=%JAVA_HOME%\bin\java.exe

:noJavaHome
IF "%_JAVACMD%" == "" SET _JAVACMD=java.exe

IF ""%1""==""debug"" SET _DEBUG=-Xdebug -Xrunjdwp:transport=dt_shmem,address=aardvark,server=y,suspend=y
IF ""%1""==""debug"" SHIFT

REM Slurp the command line arguments. This loop allows for an unlimited number
REM of arguments (up to the command line limit, anyway).
 _CMD_LINE_ARGS=vedit
:setupArgs
SET _CMD_LINE_ARGS=%_CMD_LINE_ARGS% %1
SHIFT
IF ""%1"" NEQ """" GOTO setupArgs

"%_JAVACMD%" %_DEBUG% -cp "%_LIB_DIR%\aardvark-launcher.jar";"%_LIB_DIR%\ant\ant-launcher.jar" %AARDVARK_OPTS% org.aardvark.launch.Launcher %_CMD_LINE_ARGS%

SET _JAVACMD=
SET _CMD_LINE_ARGS=
SET _DEBUG=
SET _BIN_DIR=
SET _LIB_DIR=
