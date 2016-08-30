package me.jessyan.audiomanager.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import me.jessyan.audiomanager.AudioListener;
import me.jessyan.audiomanager.AudioManager;
import me.jessyan.audiomanager.StatusListener;
import me.jessyan.audiomanager.entity.AudioInfo;
import me.jessyan.audiomanager.io.HttpHelper;
import me.jessyan.audiomanager.io.onDownloadListener;
import me.jessyan.audiomanager.io.onNetWorkListener;

public class MainActivity extends AppCompatActivity {

    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAudioManager();

    }

    private void initAudioManager() {
        mAudioManager = AudioManager
                .builder()
                .with(this)
                .minRecordTime(2, TimeUnit.SECONDS)
                .httpHelper(new HttpHelper() {//外部实现上传下载功能
                    @Override
                    public void upload(String filePath, onNetWorkListener listener) {//上传

                    }

                    @Override
                    public void download(String url, onDownloadListener listener) {//下载

                    }

                    @Override
                    public void release() {//释放一些资源

                    }
                }).addAudioListener(new AudioListener() {
                    @Override
                    public void onRecordTooShort() {//当前录音时间太短

                    }

                    @Override
                    public void onRecordTooLong() {//当前录音是时间太长

                    }

                    @Override
                    public void onVoiceAmplitudeLevel(int level) {//录音音量回调

                    }
                }).addStatusListener(new StatusListener() {
                    @Override
                    public void onStatusChange(AudioInfo info, int status) {
                        switch (status) {
                            case AudioManager.STATUS_ADD_SPEECH_LIST://添加到列表
                                break;
                            case AudioManager.STATUS_DOWNLOAD_FAILURE://下载失败
                                break;
                            case AudioManager.STATUS_DOWNLOAD_SUCCESS://下载成功
                                break;
                            case AudioManager.STATUS_DOWNLOADING://下载中
                                break;
                            case AudioManager.STATUS_PLAY_COMPLETE://播放完成
                                break;
                            case AudioManager.STATUS_PLAY_FAILURE://播放失败
                                break;
                            case AudioManager.STATUS_PLAY_PAUSE://播放停止
                                break;
                            case AudioManager.STATUS_PLAYING://播放中
                                break;
                        }
                    }
                }).build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioManager.release();
    }
}
