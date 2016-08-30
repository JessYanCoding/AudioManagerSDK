package me.jessyan.audiomanager;

import java.io.Serializable;

import me.jessyan.audiomanager.entity.AudioInfo;

/**
 * Created by jess on 7/26/16.
 */
public interface Post extends Serializable{
    void sendAudioMessage(AudioInfo info);
}
