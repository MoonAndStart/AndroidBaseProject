package com.moon.xdownload;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by chenyuelun on 2018/4/11.
 */

public abstract class XDownLoadObserver implements Observer<XDownloadInfo> {
    protected Disposable d;//可以用于取消注册的监听者
    protected XDownloadInfo downloadInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(XDownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}
