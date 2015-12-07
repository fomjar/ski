@echo off
setlocal enabledelayedexpansion

cd ..

set mod=%1
set mid=%2
if "%mod%" == "" (
	echo "Usage: %0 [mod] [mid]"
)
if "%mid%" == "" (
	echo "Usage: %0 [mod] [mid]"
)

set cp=
for /r lib %%f in (*.jar) do (
	if "!cp!" == "" (
		set "cp=%%f"
	) else (
		set "cp=!cp!;%%f"
	)
)

set opt=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8844

start /b javaw %opt% -cp %cp% com.wtcrm.%mod%.Main %mod%-%mid%

pause
exit 0
