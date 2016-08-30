package me.jessyan.audiomanager.entity;

import java.io.Serializable;

/**
 * Created by jess on 7/16/16.
 */
public class AudioInfo implements Serializable{
    public static final int NORMAL = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOAD_FAILURE = 2;
    public static final int PLAYING = 3;
    public static final int PLAY_PAUSE = 4;

    public String filePath;
    public String url;
    public int recordTime;
    public long ctime;
    public int status;//0.正常状态 1.下载中 2.播放失败 3.播放中 4.播放暂停

    public AudioInfo(){}
    public AudioInfo(String filePath, int recordTime,long ctime) {
        this.filePath = filePath;
        this.recordTime = recordTime;
        this.ctime = ctime;
    }

    public AudioInfo(String filePath, String url, int recordTime, long ctime) {
        this.filePath = filePath;
        this.url = url;
        this.recordTime = recordTime;
        this.ctime = ctime;
    }

    public AudioInfo(String filePath, String url, int recordTime, long ctime, int status) {
        this.filePath = filePath;
        this.url = url;
        this.recordTime = recordTime;
        this.ctime = ctime;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }
}
