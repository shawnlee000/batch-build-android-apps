# batch-build-android-apps
> 批量打包Android apps

## 配置
- 主程序是 BatchBuildAndroidApkMain.java，里面的全局变量要根据本地环境重新配置
    - androidAppRoot
    - gamesResourcesPath
    - apkFilesPath
- 每个游戏的app-config目录下，都必须包括该游戏的加密密码*.jks文件，会将它复制到FuriousRacingCar\app\key-store.jks，进行打包时用。
- 每个游戏的app-config目录下，build.gradle文件都要包含该游戏密钥的密码、keyAlias等，如下：
```

    signingConfigs {
        release {
            storeFile file("key-store.jks")
            storePassword "kupol666"
            keyAlias "key0"
            keyPassword "kupol666"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            debuggable false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
```
- batch-build-apk.bat文件里，需要配置安卓应用的根目录 android_app_root_path
