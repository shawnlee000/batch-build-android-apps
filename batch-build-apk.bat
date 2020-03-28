@echo off
REM 设置安卓应用根目录
set android_app_root_path=C:\code\kupol\FuriousRacingCar\
REM 进入安卓应用根目录
cd %android_app_root_path%

REM  gradlew is a .bat, need to precede with call; if it is a .exe, CALL would not be necessary
call gradlew clean
call gradlew app:assembleRelease
