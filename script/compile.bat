@echo off

if "%GRAALVM_HOME%"=="" (
    echo Please set GRAALVM_HOME
    exit /b
)

set JAVA_HOME=%GRAALVM_HOME%
set PATH=%GRAALVM_HOME%\bin;%PATH%

set /P VERSION=< resources\POD_BABASHKA_BUDDY_VERSION
echo Building version %VERSION%

if "%GRAALVM_HOME%"=="" (
echo Please set GRAALVM_HOME
exit /b
)

if not exist "classes" mkdir classes
call bb --clojure -M:native -e "(compile 'pod.babashka.buddy)"
echo "classes;" > .classpath
bb --clojure -Spath -A:native >> .classpath
set /P NATIVE_CLASSPATH=<.classpath

bb --classpath "%NATIVE_CLASSPATH%" --uberjar native.jar

call %GRAALVM_HOME%\bin\gu.cmd install native-image

call %GRAALVM_HOME%\bin\native-image.cmd ^
  "-cp" "native.jar" ^
  "-H:Name=pod-babashka-buddy" ^
  "-H:+ReportExceptionStackTraces" ^
  "--initialize-at-build-time" ^
  "-H:EnableURLProtocols=jar" ^
  "--report-unsupported-elements-at-runtime" ^
  "--verbose" ^
  "--no-fallback" ^
  "--no-server" ^
  "-J-Xmx3g" ^
  "pod.babashka.buddy"

del .classpath

if %errorlevel% neq 0 exit /b %errorlevel%

echo Creating zip archive
jar -cMf pod-babashka-biddy-%VERSION%-windows-amd64.zip pod-babashka-buddy.exe
