package com.xingen.easypermissiondemo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ${xingen} on 2017/11/1.
 * blog: http://blog.csdn.net/hexingen
 */

public class FileUtils {
    /**
     * 获得存储bitmap的文件
     * getExternalFilesDir()提供的是私有的目录，在app卸载后会被删除
     *
     * @param context
     * @param
     * @return
     */
    public static String getBitmapDiskFile(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        } else {
            cachePath = context.getFilesDir().getPath();
        }
        return new File(cachePath +File.separator+ getBitmapFileName()).getAbsolutePath();
    }

    public static final String bitmapFormat = ".png";

    /**
     * 生成bitmap的文件名:日期，md5加密
     *
     * @return
     */
    public static String getBitmapFileName() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            mDigest.update(currentDate.getBytes("utf-8"));
            byte[] b = mDigest.digest();
            for (int i = 0; i < b.length; ++i) {
                String hex = Integer.toHexString(0xFF & b[i]);
                if (hex.length() == 1) {
                    stringBuilder.append('0');
                }
                stringBuilder.append(hex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileName = stringBuilder.toString() + bitmapFormat;
        return fileName;
    }
}
