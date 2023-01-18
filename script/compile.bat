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

bb uber

call %GRAALVM_HOME%\bin\gu.cmd install native-image

call %GRAALVM_HOME%\bin\native-image.cmd ^
  "-cp" "pod-babashka-buddy.jar" ^
  "-H:Name=pod-babashka-buddy" ^
  "-H:+ReportExceptionStackTraces" ^
  "--features=graalvm.feature.BouncyCastleFeature" ^
  "--rerun-class-initialization-at-runtime=org.bouncycastle.jcajce.provider.drbg.DRBG$Default,org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV" ^
  "--report-unsupported-elements-at-runtime" ^
  "-H:ReflectionConfigurationFiles=reflection-config.json" ^
  "--verbose" ^
  "--no-fallback" ^
  "--no-server" ^
  "-J-Xmx3g" ^
  "pod.babashka.buddy"

if %errorlevel% neq 0 exit /b %errorlevel%

echo Creating zip archive
jar -cMf pod-babashka-buddy-%VERSION%-windows-amd64.zip pod-babashka-buddy.exe
