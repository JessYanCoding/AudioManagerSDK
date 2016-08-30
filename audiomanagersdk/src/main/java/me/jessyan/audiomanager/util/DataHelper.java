package me.jessyan.audiomanager.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by jess on 7/16/16.
 */
public class DataHelper {
    public static final String CACHE_FILE_PATH = "/mnt/sdcard/zhibo_cache/record";


    /**
     * 返回缓存文件夹
     */
    public static File getCacheFile(Context context) {
        File file = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = context.getExternalCacheDir();//获取系统管理的sd卡缓存文件
            if (file == null) {//如果获取的为空,就是用自己定义的缓存文件夹做缓存路径
                file = new File(CACHE_FILE_PATH);
            } else {
                file = new File(file, "record");
            }
            if (!file.exists()) {
                file.mkdirs();
            }

            return file;
        } else {
            file = new File(context.getCacheDir(), "record");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file;
        }
    }

    /**
     * 使用递归删除文件夹
     *
     * @param dir
     * @return
     */
    public static boolean DeleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                DeleteDir(file); // 递归调用继续统计
            }
        }
        return true;
    }
}
