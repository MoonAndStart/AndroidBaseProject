package com.example.base.mybase;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.moon.download.XDownloadListener;
import com.moon.download.XDownloadManager;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvProgress = findViewById(R.id.tvProgress);
        findViewById(R.id.btnDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
                    // 已经申请过权限，做想做的事
                    download();
                } else {
                    // 没有申请过权限，现在去申请
                    EasyPermissions.requestPermissions(
                            MainActivity.this,
                            "申请权限，下载文件",
                            0,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO);

                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void download() {
        String url = "http://www.caiqr.com/caiqr_default_share.apk";
        String filePath = Environment.getExternalStorageDirectory() + "/download";
        tvProgress.setText("准备下载:filePath:" + filePath);
        Log.e("mmmmm", "准备下载:filePath:" + filePath);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case 0:
                Toast.makeText(this, "已获取WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "已获取WRITE_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
