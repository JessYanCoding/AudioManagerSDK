package me.jessyan.audiomanager;

import me.jessyan.audiomanager.entity.AudioInfo;

/**
 * Created by jess on 7/21/16.
 */
public interface StatusListener<T extends AudioInfo> {
    void onStatusChange(T info, int status);
}
