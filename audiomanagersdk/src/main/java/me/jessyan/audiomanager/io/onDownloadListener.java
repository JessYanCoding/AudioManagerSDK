package me.jessyan.audiomanager.io;

/**
 * Created by jess on 7/21/16.
 */
public interface onDownloadListener {
    void onRequestSuccess(String filePath);
    void onRequestFailure();
    void onError(Throwable throwable);
}
