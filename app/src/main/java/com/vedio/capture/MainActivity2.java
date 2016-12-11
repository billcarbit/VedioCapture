package com.vedio.capture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Administrator on 2016-12-11.
 */

public class MainActivity2 extends Activity implements View.OnClickListener {

    public final static String TAG = MainActivity2.class.getSimpleName();
    private EditText np_sec_per_page, np_pic_quality, btn_file_explore;
    private Button btn_start;

    private String mCaptureInterval;
    private String mNpPicQuality;
    private SharedPreferences mSp;
    private SharedPreferences.Editor mSped;
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
        setContentView(R.layout.activity_main2);
        initView();
        initSp();
        initData();
        MainService.setHandler(myHandler);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initView() {
        np_sec_per_page = (EditText) findViewById(R.id.np_sec_per_page);
        np_pic_quality = (EditText) findViewById(R.id.np_pic_quality);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_file_explore = (EditText) findViewById(R.id.btn_file_explore);
        btn_start.setOnClickListener(this);
    }

    public void initSp() {
        mSp = getSharedPreferences("test", MODE_PRIVATE);
        mSped = mSp.edit();
    }

    private void initData() {
        mCaptureInterval = mSp.getString("np_sec_per_page", "3");
        mNpPicQuality = mSp.getString("np_pic_quality", "50");
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.btn_start:
                if(!TextUtils.equals(btn_start.getText(),"开始截图")){
                    return;
                }
                if(btn_file_explore!=null){
                   if(TextUtils.isEmpty(btn_file_explore.getText())){
                       Toast.makeText(this,"请输入路径视频存放的",Toast.LENGTH_SHORT).show();
                       break;
                   }
                }
                if (btn_start != null && !TextUtils.equals(btn_start.getText(), "开始截图")) {
                    return;
                }
                mSped.putString("np_sec_per_page", np_sec_per_page.getText().toString());
                mSped.putString("np_pic_quality", np_pic_quality.getText().toString());
                mSped.commit();

                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(new ComponentName("com.vedio.capture", "com.vedio.capture.MainService"));
                //serviceIntent.putExtra("targetFolder", Environment.getExternalStorageDirectory().getPath());
                serviceIntent.putExtra("targetFolder", btn_file_explore.getText().toString());
                serviceIntent.putExtra("npPicQuality", mNpPicQuality);
                serviceIntent.putExtra("captureInterval", mCaptureInterval);
                startService(serviceIntent);

                break;
            default:
                break;
        }
    }
}
