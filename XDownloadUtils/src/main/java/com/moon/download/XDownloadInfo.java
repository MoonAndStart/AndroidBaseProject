package com.moon.download;

/**
 * Created by chenyuelun on 2018/4/11.
 */

public class XDownloadInfo {

    public static final long TOTAL_ERROR = -1;//获取进度失败
    private String url;
    private boolean isDownLoadComplete;
    private long total;
    private long progress;
    private String fileName;
    private String fileAbsolutePath;

    public XDownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getFileAbsolutePath() {
        return fileAbsolutePath;
    }

    public void setFileAbsolutePath(String fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }

    public boolean isDownLoadComplete() {
        return isDownLoadComplete;
    }

    public void setDownLoadComplete(boolean downLoadComplete) {
        isDownLoadComplete = downLoadComplete;
    }

    @Override
    public String toString() {
        return "XDownloadInfo{" +
                "url='" + url + '\'' +
                ", isDownLoadComplete=" + isDownLoadComplete +
                ", total=" + total +
                ", progress=" + progress +
                ", fileName='" + fileName + '\'' +
                ", fileAbsolutePath='" + fileAbsolutePath + '\'' +
                '}';
    }
}
