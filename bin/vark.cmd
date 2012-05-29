@echo off
REM Copyright (c) 2012 Guidewire Software, Inc.
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

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

"%_JAVACMD%" -noverify %_DEBUG% -classpath "%_LIB_DIR%\aardvark-launcher.jar";"%_LIB_DIR%\ant-launcher.jar" %AARDVARK_OPTS% gw.vark.launch.Launcher %_CMD_LINE_ARGS%

SET _JAVACMD=
SET _CMD_LINE_ARGS=
SET _DEBUG=
SET _LIB_DIR=
SET _AARDVARK_HOME=
