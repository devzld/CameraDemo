package com.zld.camerademo;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by lingdong on 2019/3/21.
 */
public class FileUtil {

    public static File getFile() throws IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    + "ujy_camera" + File.separator);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, System.currentTimeMillis() + ".png");
            file.createNewFile();
            return file;
        }
        return null;
    }
}
