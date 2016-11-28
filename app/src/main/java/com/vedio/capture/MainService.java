package com.vedio.capture;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/28.
 */
public class MainService extends Service {

    int mCaptureInterval = 3, mNpPicQuality = 50;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        final String targetFolder = intent.getStringExtra("targetFolder");
        try {
            mCaptureInterval = Integer.valueOf(intent.getStringExtra("npPicQuality"));
            mNpPicQuality = Integer.valueOf(intent.getStringExtra("captureInterval"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                mHandler.sendEmptyMessage(1);
                File[] files = getAllFileFromDir(targetFolder);
                for (int i = 0; files != null && i < files.length; i++) {
                    getBitmapsFromVideo(files[i].getPath());
                }
                mHandler.sendEmptyMessage(2);
            }
        }.start();

        return START_STICKY;
    }

    public File[] getAllFileFromDir(String path) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        File[] tempList = file.listFiles();
        return tempList;
    }

    public void getBitmapsFromVideo(String filePath) {
        if (filePath != null && !filePath.toLowerCase().endsWith(".mp4")) {
            return;
        }
        String dataPath = filePath;
        Log.e("getBitmapsFromVideo", "dataPath=" + dataPath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        for (int i = 1; i <= seconds; i = i + mCaptureInterval) {
            Bitmap bitmap = retriever.getFrameAtTime(i * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String path = Environment.getExternalStorageDirectory() + File.separator + "_car_vedio_capture" + File.separator + i + ".jpg";
            saveBitmap(bitmap, path);
        }
    }

    public void saveBitmap(Bitmap bm, String path) {
        String dir = path.substring(0, path.lastIndexOf("/"));
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, mNpPicQuality, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Handler mHandler;

    public static void setHandler(Handler handler) {
        mHandler = handler;
    }
}
