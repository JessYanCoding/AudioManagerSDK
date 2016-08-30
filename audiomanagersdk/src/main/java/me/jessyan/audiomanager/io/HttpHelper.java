package me.jessyan.audiomanager.io;

/**
 * Created by jess on 7/18/16.
 */
public interface HttpHelper {

    void upload(String filePath, onNetWorkListener listener);

    void download(String url, onDownloadListener listener);

    void release();

}
