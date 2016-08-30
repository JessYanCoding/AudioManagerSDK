package me.jessyan.audiomanager;


import java.util.List;

import me.jessyan.audiomanager.entity.AudioInfo;

/**
 * Created by jess on 7/15/16.
 */
public interface Audio<T extends AudioInfo> {


    /**
     * 开始录音
     */
    void startRecord();

    /**
     * 结束录音
     *
     * @return 返回该录音的id
     */
    int stopRecord();

    /**
     * 取消录制
     */
    void cancel();

    /**
     * 开始上传
     */

    void startUpload(AudioInfo info, Post post);

    /**
     * 不指定具体的录音
     */
    void startUpload(Post post);

    /**
     * 是否在录制
     *
     * @return
     */
    boolean isRecord();


    /**
     * 根据id获取相应的录音信息
     */
    AudioInfo getAudioById(int id);

    /**
     * 增加到语音列表
     * @param info
     */
    void addSpeech(T info);

    /**
     * 插入语音到指定位置
     * @param position
     * @param info
     */
    void addSpeech(int position, T info);

    /**
     * 下载
     * @param info
     */
    void download(T info);
    /**
     * 获得语音列表
     * @return
     */
    List<T> getSpeechList();


    /**
     * 播放队列
     * @param info
     */
    void play(T info);

    /**
     * 播放单个录音
     * @param info
     */
    void palySingle(T info);

    /**
     * 停止播放
     */
    void stopPlay();

    /**
     * 恢复播放
     */
    void resume();



    /**
     * 释放资源
     */
    void release();


}
