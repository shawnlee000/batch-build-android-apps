package com.kupol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 批量打包安卓游戏
 */
public class BatchBuildAndroidApkMain {

    // 本地打包安卓游戏的FuriousRacingCar工程根目录
    private static String androidAppRoot = "C:\\code\\kupol\\FuriousRacingCar\\";
    // 游戏资源的根目录，每个游戏一个目录，每个游戏目录资源包括2块：app-config子目录、其他，app-config里面的资源会逐一复制到特殊位置，其他资源全部复制到assets目录；
    // 游戏目录名称，会用于打包生成的apk名称前缀
    private static String gamesResourcesPath = "C:\\code\\kupol\\Games-resources";
    // 打包后，apk会放到的位置。可以不用改
    private static String apkFilesPath = androidAppRoot + "apkFiles\\";

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmm");

    /**
     * 前置条件：将各个游戏资源放在 gamesResourcesPath 目录下，每个游戏一个目录。
     * <p>
     * 1. 打包前，会将 apkFilesPath 目录清空
     * 2. 遍历游戏目录，逐个处理游戏的资源覆盖，打包，将apk复制到指定目录，重命名apk
     * <p>
     * <p>
     * TODO: 游戏的版本号，目前还只能手工去改build.gradle文件
     */
    public static void main(String[] args) {
        purgeApkFilesPath();
        batchBuildAPKs();
    }

    private static void batchBuildAPKs() {
        File gameFolder = new File(gamesResourcesPath);
        List<File> gameFolders = Arrays.asList(gameFolder.listFiles());

        for (int i = 0; i < gameFolders.size(); i++) {
            System.out.println("【开始】打包第 " + (i + 1) + " 个游戏!!!");
            try {
                File gameFile = gameFolders.get(i);
                copyGameResourceToAndroidProject(gameFile);
                buildApk(gameFile);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("【结束-失败】打包第 " + (i + 1) + " 个游戏!!!\n\n");
                continue;
            }
            System.out.println("【结束-成功】打包第 " + (i + 1) + " 个游戏!!!\n\n");
        }
    }


    /**
     * 设置游戏的资源
     * 删除Android工程app模块下assets目录的所有资源
     * 复制该游戏的资源到assets目录
     * 复制app-config的几个特殊文件
     *
     * @param gameFile
     */
    private static void copyGameResourceToAndroidProject(File gameFile) throws Exception {
        // 删除Android工程app模块下assets目录的所有资源
        File assetsFile = new File(androidAppRoot + "app\\src\\main\\assets");
        purgeDirectory(assetsFile);

        for (File file : gameFile.listFiles()) {
            // 复制app-config目录下的资源：build.gradle, ic_launcher.jpg, strings.xml, *.jks
            if (file.getName().equals("app-config")) {
                String buildGradleFileName = "build.gradle";
                String icLauncherJpgFileName = "ic_launcher.jpg";
                String stringsXMLFileName = "strings.xml";

                File buildGradleFile = new File(file.getPath() + File.separator + buildGradleFileName);
                File icLauncherJpgFile = new File(file.getPath() + File.separator + icLauncherJpgFileName);
                File stringsXMLFile = new File(file.getPath() + File.separator + stringsXMLFileName);
                File keyStoreFile = getJKSFile(file);

                boolean hasAllAppConfigFiles = buildGradleFile.exists() && icLauncherJpgFile.exists() && stringsXMLFile.exists() && (keyStoreFile != null);
                if (!hasAllAppConfigFiles) {
                    throw new Exception(gameFile.getName() + " 游戏app-config下的资源不齐，必须包含这几个文件：build.gradle, ic_launcher.jpg, strings.xml, *.jks");
                }

                Path buildGradleSourcePath = Paths.get(buildGradleFile.toURI());
                Path buildGradleDestPath = Paths.get(androidAppRoot + "app\\" + buildGradleFileName);
                Files.copy(buildGradleSourcePath, buildGradleDestPath, StandardCopyOption.REPLACE_EXISTING);

                Path icLauncherJpgSourcePath = Paths.get(icLauncherJpgFile.toURI());
                Path icLauncherJpgDestPath = Paths.get(androidAppRoot + "app\\src\\main\\res\\drawable\\" + icLauncherJpgFileName);
                Files.copy(icLauncherJpgSourcePath, icLauncherJpgDestPath, StandardCopyOption.REPLACE_EXISTING);

                Path stringsXMLSourcePath = Paths.get(stringsXMLFile.toURI());
                Path stringsXMLDestPath = Paths.get(androidAppRoot + "app\\src\\main\\res\\values\\" + stringsXMLFileName);
                Files.copy(stringsXMLSourcePath, stringsXMLDestPath, StandardCopyOption.REPLACE_EXISTING);

                Path keyStoreSourcePath = Paths.get(keyStoreFile.toURI());
                Path keyStoreDestPath = Paths.get(androidAppRoot + "app\\key-store.jks");
                Files.copy(keyStoreSourcePath, keyStoreDestPath, StandardCopyOption.REPLACE_EXISTING);
                continue;
            }

            // 复制app-config目录外的其他资源
            Path source = Paths.get(file.toURI());
            Path dest = Paths.get(androidAppRoot + "app\\src\\main\\assets\\" + file.getName());
            Files.walkFileTree(source, new CustomFileVisitor(source, dest));
        }
    }


    /**
     * 打包apk，并将apk复制到指定目录，并重命名
     *
     * @param gameFile
     */
    private static void buildApk(File gameFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("batch-build-apk.bat");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("打包成功!");

                // 将apk复制到指定目录并重命名
                Path apkSourcePath = Paths.get(androidAppRoot + "app\\build\\outputs\\apk\\release\\app-release.apk");
                // TODO：能将游戏的 version 加到名称里更好
                String apkName = gameFile.getName() + "-" + dateTimeFormatter.format(LocalDateTime.now()) + ".apk";
                Path apkDestDestPath = Paths.get(apkFilesPath + apkName);
                Files.copy(apkSourcePath, apkDestDestPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                System.err.println("打包出错!，信息如下：");
                System.out.println(output);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取第1个 .jks 后缀的文件为密钥文件
     *
     * @param file
     * @return
     */
    private static File getJKSFile(File file) {
        File[] keyStoreFiles = file.listFiles((dir, name) -> name.endsWith(".jks"));
        File keyStoreFile = null;
        if (keyStoreFiles != null && keyStoreFiles.length > 0) {
            keyStoreFile = keyStoreFiles[0];
        }
        return keyStoreFile;
    }

    /**
     * 清空 apkFilesPath 目录所有内容；如果目录不存在，则创建
     */
    private static void purgeApkFilesPath() {
        File apkFiles = new File(apkFilesPath);
        if (apkFiles.exists()) {
            purgeDirectory(apkFiles);
        } else {
            apkFiles.mkdir();
        }
    }

    /**
     * 清空文件夹
     *
     * @param dir
     */
    private static void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

}
