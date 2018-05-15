package com.moon.xdownload;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by chenyuelun on 2018/4/11.
 */

class XIOUtil {
    static void closeAll(Closeable... closeables){
        if(closeables == null){
            return;
        }
        for (Closeable closeable : closeables) {
            if(closeable!=null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
