package com.moon.download;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chenyuelun on 2018/4/11.
 */

public class XDownloadManager {


    private static final AtomicReference<XDownloadManager> INSTANCE = new AtomicReference<>();
    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;//OKHttpClient;
    //获得一个单例类
    public static XDownloadManager getInstance() {
        for (; ; ) {
            XDownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new XDownloadManager();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private XDownloadManager() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    /**
     * 开始下载
     *
     * @param context          上下文，用于访问存储路径
     * @param url              下载请求的网址
     * @param filePath         文件的存储路径，若不指定或者因为权限或其他问题无法访问，会使用默认地址ExternalFilesDir，该路径下的文件，会随着应用卸载被删除
     * @param downloadListener 用来回调的接口
     */
    public void download(Context context, String url, String filePath, XDownloadListener downloadListener) {
        if (TextUtils.isEmpty(filePath)) {
            download(context, url, context.getExternalFilesDir(null), downloadListener);
        } else {

            download(context, url, new File(filePath), downloadListener);
        }
    }
    /**
     * 开始下载
     * 文件的存储路径，默认存在ExternalFilesDir，该路径下的文件，会随着应用卸载被删除
     * @param context          上下文，用于访问存储路径
     * @param url              下载请求的网址
     * @param downloadListener 用来回调的接口
     */
    public void download(Context context, String url, XDownloadListener downloadListener) {
        download(context, url, context.getExternalFilesDir(null), downloadListener);
    }

    /**
     * 开始下载
     *
     * @param context          上下文，用于访问存储路径
     * @param url              下载请求的网址
     * @param filePath         文件的存储路径，若不指定或者因为权限或其他问题无法访问，会使用默认地址ExternalFilesDir，该路径下的文件，会随着应用卸载被删除
     * @param downloadListener 用来回调的接口
     */
    public void download(final Context context, final String url, final File filePath, final XDownloadListener downloadListener) {
        Observable.just(url)
                .filter(new Predicate<String>() {//call的map已经有了,就证明正在下载,则这次不下载
                    @Override
                    public boolean test(String s) throws Exception {
                        return !downCalls.containsKey(s);
                    }
                })
                .flatMap(new Function<String, ObservableSource<XDownloadInfo>>() {
                    @Override
                    public ObservableSource<XDownloadInfo> apply(String s) throws Exception {
                        return Observable.just(createDownInfo(s));
                    }
                })
                .map(new Function<XDownloadInfo, XDownloadInfo>() {//检测本地文件夹,生成新的文件名
                    @Override
                    public XDownloadInfo apply(XDownloadInfo downloadInfo) throws Exception {
                        return getRealFileName(context, filePath, downloadInfo);
                    }
                })
                .flatMap(new Function<XDownloadInfo, ObservableSource<XDownloadInfo>>() {//下载
                    @Override
                    public ObservableSource<XDownloadInfo> apply(XDownloadInfo downloadInfo) throws Exception {
                        return Observable.create(new DownloadSubscribe(downloadInfo));
                    }
                })
                .subscribeOn(Schedulers.io())//在子线程执行
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribe(new XDownLoadObserver() {//添加观察者
                    @Override
                    public void onComplete() {
                        if (downloadListener != null) {
                            downloadListener.onDownloadSuccess(downloadInfo.getFileAbsolutePath());
                        }
                    }

                    @Override
                    public void onNext(XDownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        if (downloadListener != null) {
                            downloadListener.onProgress(
                                    downloadInfo.getProgress(),
                                    downloadInfo.getTotal(),
                                    (int) (downloadInfo.getProgress() * 100 / downloadInfo.getTotal()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (downloadListener != null){
                            downloadListener.onError(e);
                        }
                    }
                });

    }

    /**
     * 下载apk安装包并调起安装程序
     *
     * @param context              上下文，用于访问下载存储路径、唤醒安装程序等
     * @param url                  下载链接
     * @param filePath             指定文件的下载地址，如访问外置存储卡，请注意权限申请
     * @param xDownloadListener 下载进度监听
     */
    public void downloadApkAndInstall(final Context context, String url, File filePath, final XDownloadListener xDownloadListener) {
        download(context, url, filePath, new XDownloadListener() {

            @Override
            public void onProgress(long progress, long totalProgress, int percent) {
                if (xDownloadListener != null) {
                    xDownloadListener.onProgress(progress, totalProgress, percent);
                }
            }

            @Override
            public void onDownloadSuccess(String filePath) {
                if (xDownloadListener != null) {
                    xDownloadListener.onDownloadSuccess(filePath);
                }
                if (filePath.endsWith(".apk")) {
                    File file = new File(filePath);
                    if (file.exists())
                        install(context, file);
                }
            }

            @Override
            public void onError(Throwable ex) {
                if (xDownloadListener != null) {
                    xDownloadListener.onError(ex);
                }
            }

        });
    }

    /**
     * 下载apk安装包并调起安装程序
     *
     * @param context              上下文 用于访问下载路径,唤醒安装程序
     * @param url                  下载链接
     * @param xDownloadListener 下载安装进度监听
     */
    public void downloadApkAndInstall(Context context, String url, XDownloadListener xDownloadListener) {
        downloadApkAndInstall(context, url, context.getExternalFilesDir(null), xDownloadListener);
    }

    public void downloadApkAndInstall(Context context, String url, String filePath,XDownloadListener xDownloadListener ) {
        if (TextUtils.isEmpty(filePath)) {
            downloadApkAndInstall(context, url, context.getExternalFilesDir(null), xDownloadListener);
        } else {

            downloadApkAndInstall(context, url, new File(filePath), xDownloadListener);
        }
    }



    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private XDownloadInfo createDownInfo(String url) {
        XDownloadInfo downloadInfo = new XDownloadInfo(url);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        String fileName = url.substring(url.lastIndexOf("/"));
        downloadInfo.setFileName(fileName);
        return downloadInfo;
    }

    private XDownloadInfo getRealFileName(Context context, File filePath, XDownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        //如果未指定文件夹或者指定的文件夹不能读写 使用默认文件夹

        if (filePath == null) {
            //|| !filePath.canRead() || !filePath.canWrite()
            filePath = context.getExternalFilesDir(null);
        } else {
            if (!filePath.exists()) {
                boolean mkdirs = filePath.mkdirs();
                if (!mkdirs) {
                    filePath = context.getExternalFilesDir(null);
                }
            }
        }

        if (filePath == null || !filePath.canWrite() || !filePath.canRead()) {
            filePath = context.getExternalFilesDir(null);
        }

        File file = new File(filePath, fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        //比较将要下载的文件和将要下载的文件，是否是同一个
        if (downloadLength == contentLength) {
            //如果两个文件大小相同，默认是同一个，此处有问题，只比较长度不太可靠 待优化
            downloadInfo.setDownLoadComplete(true);
        } else {
            downloadInfo.setDownLoadComplete(false);
            //之前下载过,需要重新来一个文件
            int i = 1;
            while (downloadLength >= contentLength) {
                int dotIndex = fileName.lastIndexOf(".");
                String fileNameOther;
                if (dotIndex == -1) {
                    fileNameOther = fileName + "(" + i + ")";
                } else {
                    fileNameOther = fileName.substring(0, dotIndex)
                            + "(" + i + ")" + fileName.substring(dotIndex);
                }
                File newFile = new File(filePath, fileNameOther);
                file = newFile;
                downloadLength = newFile.length();
                i++;
            }
        }

        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        downloadInfo.setFileAbsolutePath(file.getAbsolutePath());
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<XDownloadInfo> {
        private XDownloadInfo downloadInfo;

        DownloadSubscribe(XDownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<XDownloadInfo> e) throws Exception {
            //文件不存在 下载
            if (!downloadInfo.isDownLoadComplete()) {
                String url = downloadInfo.getUrl();
                long downloadLength = downloadInfo.getProgress();//已经下载好的长度
                long contentLength = downloadInfo.getTotal();//文件的总长度
                //初始进度信息
                e.onNext(downloadInfo);

                Request request = new Request.Builder()
                        //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                        .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                        .url(url)
                        .build();
                Call call = mClient.newCall(request);
                downCalls.put(url, call);//把这个添加到call里,方便取消
                Response response = call.execute();

                File file = new File(downloadInfo.getFileAbsolutePath());
                InputStream is = null;
                FileOutputStream fileOutputStream = null;
                try {
                    is = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file, true);
                    byte[] buffer = new byte[2048];//缓冲数组2kB
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        downloadLength += len;
                        downloadInfo.setProgress(downloadLength);
                        e.onNext(downloadInfo);
                    }
                    fileOutputStream.flush();
                    downCalls.remove(url);
                } catch (Exception ex) {
                    e.onError(ex);
                } finally {
                    //关闭IO流
                    XIOUtil.closeAll(is, fileOutputStream);
                }
            } else {
                downloadInfo.setProgress(100);
                e.onNext(downloadInfo);
            }
            e.onComplete();//完成
        }

    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? XDownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return XDownloadInfo.TOTAL_ERROR;
    }

    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    private void install(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationInfo().processName + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
