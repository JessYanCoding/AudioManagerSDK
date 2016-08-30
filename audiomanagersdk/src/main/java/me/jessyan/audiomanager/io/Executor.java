package me.jessyan.audiomanager.io;

import me.jessyan.audiomanager.Post;
import me.jessyan.audiomanager.entity.AudioInfo;

/**
 * Created by jess on 8/23/16 16:12
 * Contact with jess.yan.effort@gmail.com
 */
public interface Executor {
    /**
     * 开始执行
     * @param info
     * @param post
     */
    void start(AudioInfo info, Post post);
    /**
     * 停止
     */
    void stop();
}
