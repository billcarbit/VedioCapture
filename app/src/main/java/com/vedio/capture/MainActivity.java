package com.vedio.capture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText np_sec_per_page, np_pic_quality;
    private Button btn_file_explore, btn_start;

    private SharedPreferences sp;
    private SharedPreferences.Editor sped;

    private String mCaptureInterval;
    private String mNpPicQuality;
    private String mFolder;


    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    btn_start.setText("正在将该文件夹内的MP4转换为图片...");
                    break;
                case 2:
                    btn_start.setText("转换完成");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initSp();
        initOriginData();
        MainService.setHandler(myHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btn_file_explore != null && !TextUtils.isEmpty(mFolder)) {
            btn_file_explore.setText(mFolder);
        }
        if (np_sec_per_page != null) {
            np_sec_per_page.setText(sp.getString("np_sec_per_page", "3"));
        }
        if (np_pic_quality != null) {
            np_pic_quality.setText(sp.getString("np_pic_quality", "50"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Get the Uri of the selected file
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            if (actualimagecursor == null) {
                return;
            }
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String storePath = actualimagecursor.getString(actual_image_column_index);
            if (storePath == null) {
                return;
            }
            Log.e("onActivityResult", "storePath=" + storePath);
            String folder = storePath.substring(0, storePath.lastIndexOf("/"));
            Log.e("onActivityResult", "storePath=" + storePath + ",folder=" + folder + ",aaa=" + Environment.getExternalStorageDirectory());
            mFolder = folder;
            if (btn_file_explore == null) {
                return;
            }
            btn_file_explore.setText(folder);

        }
    }

    public void initView() {
        np_sec_per_page = (EditText) findViewById(R.id.np_sec_per_page);
        np_pic_quality = (EditText) findViewById(R.id.np_pic_quality);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_file_explore = (Button) findViewById(R.id.btn_file_explore);
    }

    public void initListener() {
        btn_start.setOnClickListener(mBtnStartOnClickListener);
        btn_file_explore.setOnClickListener(mBtnFileExploreOnClickListener);
    }

    public void initSp() {
        sp = getSharedPreferences("test", MODE_PRIVATE);
        sped = sp.edit();
    }

    private void initOriginData() {
        mCaptureInterval = sp.getString("np_sec_per_page", "3");
        mNpPicQuality = sp.getString("np_pic_quality", "50");
    }


    //开始截图
    private View.OnClickListener mBtnStartOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (btn_start != null && !TextUtils.equals(btn_start.getText(), "开始截图")) {
                return;
            }
            sped.putString("np_sec_per_page", np_sec_per_page.getText().toString());
            sped.putString("np_pic_quality", np_pic_quality.getText().toString());
            sped.commit();

            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName("com.vedio.capture", "com.vedio.capture.MainService"));
            serviceIntent.putExtra("targetFolder", mFolder);
            serviceIntent.putExtra("npPicQuality", mNpPicQuality);
            serviceIntent.putExtra("captureInterval", mCaptureInterval);
            startService(serviceIntent);
        }
    };


    //文件浏览
    private View.OnClickListener mBtnFileExploreOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openSystemFile();
        }
    };


    public void openSystemFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择文件!"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land do nothing is ok
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
        }

    }
}
