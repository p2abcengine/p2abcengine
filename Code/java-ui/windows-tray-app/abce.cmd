@echo off

cd "%PROGRAMFILES%\ABC4Trust\User Client"
if %ERRORLEVEL%== goto foundPROGRAMFILES
cd "%programfiles(x86)%\ABC4Trust\User Client"
:foundPROGRAMFILES

set "JAVA_OPTS=-Xmx256M -Djava.net.preferIPv4Stack=true -Xrs"
set "JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
set "JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8"

REM - detect java command...
java.exe 2> NUL
if not %ERRORLEVEL%==9009 goto javaFoundInPath

echo java.exe NOT FOUND IN path
REM set "JAVA_EXE=%PROGRAMFILES%\Java\jre7\bin\java.exe"
REM if exist "%JAVA_EXE%" goto detectCommand
REM set "JAVA_EXE=%PROGRAMFILES% (x86)\Java\jre7\bin\java.exe"
REM if exist "%JAVA_EXE%" goto detectCommand
REM echo "JAVA NOT FOUND!"
REM goto cmdEnd

ECHO detect in REGISTRY
SET KIT=JavaSoft\Java Runtime Environment
call:ReadRegValue VER "HKLM\Software\%KIT%" "CurrentVersion"
IF "%VER%" NEQ "" GOTO FoundJRE

SET KIT=Wow6432Node\JavaSoft\Java Runtime Environment
call:ReadRegValue VER "HKLM\Software\%KIT%" "CurrentVersion"
IF "%VER%" NEQ "" GOTO FoundJRE

SET KIT=JavaSoft\Java Development Kit
call:ReadRegValue VER "HKLM\Software\%KIT%" "CurrentVersion"
IF "%VER%" NEQ "" GOTO FoundJRE

SET KIT=Wow6432Node\JavaSoft\Java Development Kit
call:ReadRegValue VER "HKLM\Software\%KIT%" "CurrentVersion"
IF "%VER%" NEQ "" GOTO FoundJRE

ECHO Failed to find Java
GOTO :EOF

:ReadRegValue
SET key=%2%
SET name=%3%
SET "%~1="
SET reg=reg
IF DEFINED ProgramFiles(x86) (
  IF EXIST %WINDIR%\sysnative\reg.exe SET reg=%WINDIR%\sysnative\reg.exe
)
FOR /F "usebackq tokens=3* skip=1" %%A IN (`%reg% QUERY %key% /v %name% 2^>NUL`) DO SET "%~1=%%A %%B"
GOTO :EOF


:FoundJRE
call:ReadRegValue JAVAPATH "HKLM\Software\%KIT%\%VER%" "JavaHome"
ECHO Found JAVAPATH in Registry : %JAVAPATH%
set JAVA_EXE="%JAVAPATH%\bin\java.exe"
goto detectCommand


:javaFoundInPath
echo found java.exe in path
set JAVA_EXE=java.exe
goto detectCommand


echo "JAVA EXE == %JAVA_EXE%"

:detectCommand
if /I "%1" == "start" goto cmdStart
if /I "%1" == "startlog" goto cmdStartLog
if /I "%1" == "stop"  goto cmdStop
echo Usage: abce start^|stop
goto cmdEnd

:cmdStart
echo JAVA EXE == %JAVA_EXE%
call %JAVA_EXE% %JAVA_OPTS% -jar user-service-executable.jar start
goto cmdEnd

:cmdStartLog
echo STARTLOG JAVA EXE == "%JAVA_EXE%
call %JAVA_EXE% %JAVA_OPTS% -jar user-service-executable.jar start > %HOME%\AppData\Local\ABC4Trust\runlog.txt
goto cmdEnd

:cmdStop
echo JAVA EXE == %JAVA_EXE%
call %JAVA_EXE% -jar user-service-executable.jar stop
goto cmdEnd

:cmdEnd
