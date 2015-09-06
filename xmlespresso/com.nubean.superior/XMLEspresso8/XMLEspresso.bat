@echo off 
set JDK17_Version=1.7.0
set JDK16_Version=1.6.0

echo. 
echo Locating JDK %JDK17_Version% 
 
for /d %%i in ("%ProgramFiles%\Java\jdk%JDK17_Version%*") do set Located="%%i\bin\java"
if X%Located%==X goto else 
echo Located  %Located%
set WORKDIR=%~dp0
set CLASSPATH=%WORKDIR%;%WORKDIR%michide.jar;%WORKDIR%dtdparser.jar;%WORKDIR%javacc.jar;%WORKDIR%jh.jar;%WORKDIR%jsearch.jar;%WORKDIR%jlfgr-1_0.jar;%WORKDIR%iText-5.0.2.jar;%WORKDIR%jhall.jar;%WORKDIR%jlfgr-1_0.jar
%Located% -Xms32m -Xmx512m com.nubean.michlic.MichiganLauncher %1

goto endif 
 
:else
echo Locating JDK %JDK16_Version% 
for /d %%i in ("%ProgramFiles%\Java\jdk%JDK16_Version%*") do set Located="%%i\bin\java"
if X%Located%==X goto NoExistingJavaHome 
echo Located  %Located%
set WORKDIR=%~dp0
set CLASSPATH=%WORKDIR%;%WORKDIR%michide.jar;%WORKDIR%dtdparser.jar;%WORKDIR%javacc.jar;%WORKDIR%jh.jar;%WORKDIR%jsearch.jar;%WORKDIR%jlfgr-1_0.jar;%WORKDIR%iText-5.0.2.jar;%WORKDIR%jhall.jar;%WORKDIR%jlfgr-1_0.jar
%Located% -Xms32m -Xmx512m com.nubean.michlic.MichiganLauncher %1

goto endif
 
:NoExistingJavaHome 
echo     No Existing value of JAVA_HOME version %JDK_Version% is available 
echo Please download and install Java 6 from http://www.oracle.com/technetwork/java/javase/downloads/index.html
goto endif 
 
:endif 
set JDK_Version= 
set Located= 