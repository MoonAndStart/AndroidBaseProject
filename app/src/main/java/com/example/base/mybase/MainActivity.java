package com.example.base.mybase;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.moon.download.XDownloadListener;
import com.moon.download.XDownloadManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvProgress = findViewById(R.id.tvProgress);
        findViewById(R.id.btnDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
    }

    private void download() {
        String url = "http://www.caiqr.com/caiqr_default_share.apk";
        String filePath = Environment.getExternalStorageDirectory() + "/download";
        tvProgress.setText("准备下载:filePath:" + filePath);
        Log.e("mmmmm","准备下载:filePath:" + filePath);
        XDownloadManager.getInstance().downloadApkAndInstall(this, url, filePath, new XDownloadListener() {

            @Override
            public void onProgress(long progress, long totalProgress, int percent) {
                tvProgress.setText(progress + "/" + totalProgress + "___" + percent + "%");
            }

            @Override
            public void onDownloadSuccess(String filePath) {
                tvProgress.setText("下载完成:filePath:" + filePath);
            }

            @Override
            public void onError(Throwable ex) {
                tvProgress.setText("下载出错了" + ex.getMessage());
            }
        });
//        XDownloadManager.getInstance().download(this, url,filePath, new XDownloadListener() {
//            @Override
//            public void onStart() {
//                tvProgress.setText("0");
//            }
//
//            @Override
//            public void onProgress(long progress, long totalProgress, int percent) {
//                tvProgress.setText(progress + "/" + totalProgress + "___" + percent + "%");
//            }
//
//            @Override
//            public void onDownloadSuccess(String filePath) {
//                tvProgress.setText("下载完成:filePath:" + filePath);
//                Log.e("mmmmm","下载完成:filePath:" + filePath);
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//                ex.printStackTrace();
//                tvProgress.setText("下载出错了" + ex.getMessage());
//            }
//        });

    }
}
