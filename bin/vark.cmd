@echo off
REM Copyright (c) 2010 Guidewire Software, Inc.

setLocal EnableDelayedExpansion

SET _DEBUG=
SET _CMD_LINE_ARGS=

IF "%AARDVARK_HOME%" == "" GOTO noAardvarkHome
IF NOT EXIST "%AARDVARK_HOME%\lib\aardvark-launcher.jar" GOTO noAardvarkHome
SET _AARDVARK_HOME=%AARDVARK_HOME%
GOTO doneAardvarkHome
:noAardvarkHome
SET _AARDVARK_HOME=%~dp0..
:doneAardvarkHome

SET _LIB_DIR=%_AARDVARK_HOME%\lib

SET _JAVACMD=%JAVACMD%
IF "%_JAVACMD%" NEQ "" GOTO doneJavaCmd
IF "%JAVA_HOME%" == "" GOTO noJavaHome
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" GOTO noJavaHome
SET _JAVACMD=%JAVA_HOME%\bin\java.exe
GOTO doneJavaCmd
:noJavaHome
SET _JAVACMD=java.exe
:doneJavaCmd

IF ""%1""==""debug"" SET _DEBUG=-Xdebug -Xrunjdwp:transport=dt_shmem,address=aardvark,server=y,suspend=y
IF ""%1""==""debug"" SHIFT

REM Slurp the command line arguments. This loop allows for an unlimited number
REM of arguments (up to the command line limit, anyway).
:setupArgs
SET _CMD_LINE_ARGS=%_CMD_LINE_ARGS% %1
SHIFT
IF ""%1"" NEQ """" GOTO setupArgs

"%_JAVACMD%" %_DEBUG% -cp "%_LIB_DIR%\aardvark-launcher.jar";"%_LIB_DIR%\ant\ant-launcher.jar" %AARDVARK_OPTS% gw.vark.launch.Launcher %_CMD_LINE_ARGS%

SET _JAVACMD=
SET _CMD_LINE_ARGS=
SET _DEBUG=
SET _LIB_DIR=
SET _AARDVARK_HOME=
