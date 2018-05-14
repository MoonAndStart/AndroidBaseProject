package com.moon.download;

/**
 * Created by chenyuelun on 2018/5/10.
 */

public interface XDownloadListener {

    void onProgress(long progress,long totalProgress,int percent);

    void onDownloadSuccess(String filePath);

    void onError(Throwable ex);
}
