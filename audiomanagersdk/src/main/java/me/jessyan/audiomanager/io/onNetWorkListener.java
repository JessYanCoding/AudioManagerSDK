package me.jessyan.audiomanager.io;

/**
 * Created by jess on 7/20/16.
 */
public interface onNetWorkListener {
    void onRequestSuccess(String url);
    void onRequestFailure();
    void onError(Throwable throwable);
}
