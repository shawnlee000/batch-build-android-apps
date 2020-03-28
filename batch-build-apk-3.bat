@echo off
REM 安卓应用根目录
set android_app_root_path=C:\code\kupol\FuriousRacingCar\
REM get into the android app root path
cd %android_app_root_path%

REM 在安卓应用根目录，生成2个目录，临时文件目录 0_temp_file ，apk目录 0_apk_file。如果已经存在目录，会先删除
set apk_file_path=%android_app_root_path%0_apk_file\
rd /Q /S %apk_file_path%
md %apk_file_path%


REM build the apk, 如果 app-release-unsigned.apk 文件已经存在，gradlew assemble 不会重新生成 apk，需要生执行 clean
REM  gradlew is a .bat, need to precede with call; if it is a .exe, CALL would not be necessary
REM call gradlew clean
REM call gradlew app:assembleRelease

REM 将 app-release.apk 复制到 apk_file_path 目录
copy %android_app_root_path%app\build\outputs\apk\release\app-release.apk %apk_file_path%\app-release-rename-%DATE%.apk

REM dir /ad
REM This can output just the name %%~nxD or the full path %%~fD
REM 必须声明局部变量，才能在for里面使用set
setlocal EnableDelayedExpansion
for /d %%D in (*) do (    
    REM echo %%~nxD
    set folder_name=%%~nxD
    REM for里面的变量要使用 !! 包起来才能用，外面的使用 %% 包起来，好奇怪的语法
    echo !folder_name!
)
endlocal