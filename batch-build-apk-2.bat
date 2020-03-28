REM Build the APK:    gradlew.bat assemble
REM C:\Users\ShawnLee\AppData\Local\Android\Sdk\build-tools\28.0.3>.\zipalign.exe
REM .\apksigner.bat
REM ZipAlign the APK: .\zipalign.exe -v -p 4 D:\apk\app-release-unsigned.apk d:\apk\app-release-unsigned-aligned.apk
REM Sign The APK:.\apksigner.bat sign --ks D:\apk\swift-ninja.jks --out D:\apk\app-release-signed.apk D:\apk\app-release-unsigned-aligned.apk


@echo off
REM 安卓应用根目录
set android_app_root_path=C:\code\kupol\FuriousRacingCar\
REM 安卓工具目录， zipalign.exe apksigner.bat
set android_tool_path=C:\Users\ShawnLee\AppData\Local\Android\Sdk\build-tools\28.0.3\

set zipalign_exe=%android_tool_path%zipalign.exe
set apksigner_bat=%android_tool_path%apksigner.bat

REM get into the android app root path
cd %android_app_root_path%

REM 在安卓应用根目录，生成2个目录，临时文件目录 0_temp_file ，apk目录 0_apk_file。如果已经存在目录，会先删除
set temp_file_path=%android_app_root_path%0_temp_file\
set apk_file_path=%android_app_root_path%0_apk_file\
rd /Q /S %temp_file_path%, %apk_file_path%
md %temp_file_path%, %apk_file_path%


REM build the apk, 如果 app-release-unsigned.apk 文件已经存在，gradlew assemble 不会重新生成 apk，需要生执行 clean
REM  gradlew is a .bat, need to precede with call; if it is a .exe, CALL would not be necessary
call gradlew clean
call gradlew app:assembleRelease

REM 生成的 unsigned apk 在： %android_app_root_path%app\build\outputs\apk\release\app-release-unsigned.apk
set unsigned_apk_path=%android_app_root_path%app\build\outputs\apk\release\app-release-unsigned.apk
set unsigned_aligned_apk_path=%temp_file_path%app-release-unsigned-aligned.apk
REM zipalign apk to unsigned_aligned_apk_path
%zipalign_exe% -v -p 4 %unsigned_apk_path% %unsigned_aligned_apk_path%

REM 生成加密的 apk 
set signed_apk_path=%apk_file_path%app-release.apk
REM 这里还要手动输入之前设定的密码
call %apksigner_bat% sign --ks D:\apk\swift-ninja.jks --out %signed_apk_path% %unsigned_aligned_apk_path%