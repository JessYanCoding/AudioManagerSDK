package me.jessyan.audiomanager;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAmplitude;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import me.jessyan.audiomanager.entity.AudioInfo;
import me.jessyan.audiomanager.io.Executor;
import me.jessyan.audiomanager.io.HttpExecutor;
import me.jessyan.audiomanager.io.HttpHelper;
import me.jessyan.audiomanager.io.onDownloadListener;
import me.jessyan.audiomanager.io.onNetWorkListener;
import me.jessyan.audiomanager.util.DataHelper;
import me.jessyan.audiomanager.util.LogUtils;
import me.jessyan.audiomanager.util.UiUitls;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by jess on 7/15/16.
 */
public class AudioManager<T extends AudioInfo> implements Audio<T>, AudioRecorder.OnErrorListener {

    private final String TAG = getClass().getSimpleName();
    private AudioRecorder mAudioRecorder;
    private RxAudioPlayer mRxAudioPlayer;
    private boolean isRecord;
    private Context mContext;
    private File mCurrentFile;
    private Queue<AudioInfo> mUploadFiles = new LinkedList<>();
    private Queue<T> mDownloadFiles = new LinkedList<>();
    private List<AudioInfo> mNativeFiles = new ArrayList<>();//记录所有录制成功的audio
    private List<T> mSpeechList = new ArrayList<>();//所有收到的列表
    private Queue<T> mPlayQueue = new LinkedList<>();//播放音频的队列
    private int mMinRecordTime;
    private int mMaxRecordTime;
    private AudioListener mListener;
    private Subscription mSubscribe;
    private HttpHelper mHttpHelper;
    private Executor mExecutor;
    private StatusListener mStatusListener;
    private T mCurrentPlay;
    private boolean isPlaying;
    private boolean isStop;


    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_DOWNLOAD_FAILURE = 1;
    public static final int STATUS_DOWNLOAD_SUCCESS = 2;
    public static final int STATUS_PLAYING = 3;
    public static final int STATUS_PLAY_FAILURE = 4;
    public static final int STATUS_PLAY_COMPLETE = 5;
    public static final int STATUS_ADD_SPEECH_LIST = 6;
    public static final int STATUS_PLAY_PAUSE = 7;


    public static final int HANDLE_STATUS_TASK = 0;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_STATUS_TASK:
                    T info = (T) msg.obj;
                    if (mStatusListener != null)
                        mStatusListener.onStatusChange(info, msg.arg1);
                    break;
            }
        }
    };
    private Subscription mPlaySubscription;
    private Subscription mSingleSubscription;


    private AudioManager(Context context, int maxRecordTime, int minRecordTime
            , AudioListener listener, HttpHelper httpHelper, StatusListener statusListener, Executor executor) {
        this.mContext = context;
        this.mMaxRecordTime = maxRecordTime;
        this.mMinRecordTime = minRecordTime;
        this.mListener = listener;
        this.mHttpHelper = httpHelper;
        this.mStatusListener = statusListener;
        mAudioRecorder = AudioRecorder.getInstance();
        mRxAudioPlayer = RxAudioPlayer.getInstance();
        mAudioRecorder.setOnErrorListener(this);
        mExecutor = executor;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public synchronized void startRecord() {
        this.isRecord = true;//标记
        mCurrentFile = new File(DataHelper.getCacheFile(mContext), System.nanoTime() + ".file.m4a");

//        mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC,
//                MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
//                192000, 192000, mCurrentFile);
        mSubscribe = Observable.just(1)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC,
                                MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
                                192000, 192000, mCurrentFile);
                        return null;
                    }
                }).flatMap(new Func1<Object, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Object o) {
                        return Observable.timer(1, TimeUnit.MILLISECONDS);
                    }
                }).map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        mAudioRecorder.startRecord();//开始录音
                        return null;
                    }
                }).flatMap(new Func1<Object, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Object o) {
                        return RxAmplitude.from(mAudioRecorder);
                    }
                }).subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer level) {
                        if (mListener != null) {
                            mListener.onVoiceAmplitudeLevel(level);
//                            LogUtils.warnInfo(TAG, mAudioRecorder.progress() + "s");
                            if (mMaxRecordTime != -1) {
                                if (mAudioRecorder.progress() == mMaxRecordTime) {
                                    mListener.onRecordTooLong();
                                }
                            }
                        }
                    }
                });


//        mSubscribe = RxAmplitude.from(mAudioRecorder)
//                .subscribe(new Action1<Integer>() {
//                    @Override
//                    public void call(Integer level) {
//                        if (mListener != null) {
//                            mListener.onVoiceAmplitudeLevel(level);
//                            if (mMaxRecordTime != -1) {
//                                if (mAudioRecorder.progress() == mMaxRecordTime) {
//                                    mListener.onRecordTooLong();
//                                }
//
//                            }
//                        }
//                    }
//                });
    }

    @Override
    public synchronized int stopRecord() {
        if (!isRecord)
            throw new IllegalStateException("not startRecord");
        else
            this.isRecord = false;

        int record = mAudioRecorder.stopRecord();
        if (record == -1) {//-1说明录制失败
            UiUitls.makeText(mContext, "录制发生错误");
            return -1;
        }
        Log.w(TAG, record + "========");
        if (mListener != null && mMinRecordTime != -1) {//录制时间太少
            if (record >= 0 && record <= mMinRecordTime) {
                mListener.onRecordTooShort();
                return -1;
            }
        }
        AudioInfo audioInfo = new AudioInfo(mCurrentFile.getAbsolutePath(), record, System.currentTimeMillis());
        mUploadFiles.offer(audioInfo);
        mNativeFiles.add(audioInfo);
        return mNativeFiles.indexOf(audioInfo);//返回录制的时间
    }


    /**
     * 取消录制
     */
    @Override
    public synchronized void cancel() {
        if (!isRecord)
            throw new IllegalStateException("not startRecord");
        else
            this.isRecord = false;
        mAudioRecorder.stopRecord();
    }

    /**
     * 开始上传
     *
     * @param info
     */
    @Override
    public void startUpload(AudioInfo info, Post post) {
        upload(info, post);//开始上传
    }


    @Override
    public void startUpload(Post post) {
        AudioInfo info = mUploadFiles.poll();
        LogUtils.warnInfo(TAG, "upload_start------->" + info);
        if (info == null) return;
        upload(info, post);//开始上传
    }


    private void upload(final AudioInfo info, final Post post) {
        mHttpHelper.upload(info.filePath, new onNetWorkListener() {
            @Override
            public void onRequestSuccess(String url) {
                LogUtils.warnInfo(TAG, "success--------------->");
                info.url = url;
                post.sendAudioMessage(info);//发送录音信息给主播
            }

            @Override
            public void onRequestFailure() {
                LogUtils.warnInfo(TAG, "failure--------------->");
                mUploadFiles.offer(info);
                mExecutor.start(info, post);//失败重新上传
            }

            @Override
            public void onError(Throwable throwable) {
                LogUtils.warnInfo(TAG, "error--------------->");
                mUploadFiles.offer(info);
                mExecutor.start(info, post);//失败重新上传
            }
        });
    }

    @Override
    public boolean isRecord() {
        return isRecord;
    }

    @Override
    public AudioInfo getAudioById(int id) {
        return mNativeFiles.get(id);
    }

    @Override
    public void addSpeech(T info) {
        this.mSpeechList.add(info);
        SendStatusTask(info, STATUS_ADD_SPEECH_LIST);
    }

    @Override
    public void addSpeech(int position, T info) {
        this.mSpeechList.add(position, info);
        SendStatusTask(info, STATUS_ADD_SPEECH_LIST);
    }

    @Override
    public void download(final T info) {
        info.status = AudioInfo.DOWNLOADING;
        SendStatusTask(info, STATUS_DOWNLOADING);

        mHttpHelper.download(info.url, new onDownloadListener() {
            @Override
            public void onRequestSuccess(String filePath) {
                info.filePath = filePath;
                info.status = AudioInfo.NORMAL;
                SendStatusTask(info, STATUS_DOWNLOAD_SUCCESS);
            }

            @Override
            public void onRequestFailure() {
                info.status = AudioInfo.DOWNLOAD_FAILURE;
                SendStatusTask(info, STATUS_DOWNLOAD_FAILURE);
            }

            @Override
            public void onError(Throwable throwable) {
                info.status = AudioInfo.DOWNLOAD_FAILURE;
                SendStatusTask(info, STATUS_DOWNLOAD_FAILURE);
            }
        });
    }

    /**
     * 发送执行改变状态的信息
     *
     * @param info
     * @param status
     */
    private void SendStatusTask(T info, int status) {
        Message message = mHandler.obtainMessage();
        message.what = HANDLE_STATUS_TASK;
        message.obj = info;
        message.arg1 = status;
        mHandler.sendMessage(message);
    }


    @Override
    public void release() {
        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            mSubscribe.unsubscribe();
            mSubscribe = null;
        }
        if (mSingleSubscription != null && !mSingleSubscription.isUnsubscribed()) {
            mSingleSubscription.unsubscribe();
            mSingleSubscription = null;
        }

        this.mCurrentFile = null;
        mListener = null;
        mStatusListener = null;
        mHttpHelper.release();
        mExecutor.stop();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        // TODO: 7/19/16 清理所有缓存到本地的录音文件
        DataHelper.DeleteDir(DataHelper.getCacheFile(mContext));
    }

    @Override
    public List<T> getSpeechList() {
        return mSpeechList;
    }

    /**
     * 播放队列里面的录音
     *
     * @param playInfo
     */
    @Override
    public synchronized void play(final T playInfo) {
        if (isStop) {//如果当前为停止状态,则放入队列
            LogUtils.warnInfo(TAG, "------------>stop");
            mPlayQueue.offer(playInfo);
        } else {//否则执行播放
            LogUtils.warnInfo(TAG, "------------>play queue");
            startPlayQueue(playInfo);
        }
    }

    /**
     * 播放单个语音,播放前停止所有正在执行的录音
     *
     * @param info
     */
    @Override
    public synchronized void palySingle(final T info) {
        stopPlay();//播放前停止所有正在执行的录音
        File file = new File(info.filePath);
        if (!file.exists()) {
            throw new IllegalStateException("audio file isn't exist");
        }
        mCurrentPlay = info;//当前正在执行的录音

        info.status = AudioInfo.PLAYING;
        SendStatusTask(info, STATUS_PLAYING);//播放中

        mSingleSubscription = mRxAudioPlayer.play(PlayConfig.file(file).build())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.immediate())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        isStop = false;
                        LogUtils.warnInfo(TAG, "----------palySingle success------------");
                        info.status = AudioInfo.NORMAL;
                        SendStatusTask(info, STATUS_PLAY_COMPLETE);//播放完成

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        mPlayQueue.offer(info);//发生错误储存这个发生错误的录音
                        isStop = false;
                        LogUtils.warnInfo(TAG, "----------palySingle erro------------");
                        info.status = AudioInfo.NORMAL;
                        SendStatusTask(info, STATUS_PLAY_FAILURE);//播放失败
                    }
                });
    }

    /**
     * 开始播放队列里的录音
     *
     * @param info
     */
    private synchronized void startPlayQueue(final T info) {
        if (!isPlaying) {//没有正在播放的音频,才能播放下一个
            isPlaying = true;
            File file = new File(info.filePath);
            if (!file.exists()) {
                throw new IllegalStateException("audio file isn't exist");
            }
            mCurrentPlay = info;//当前播放的录音

            info.status = AudioInfo.PLAYING;
            SendStatusTask(info, STATUS_PLAYING);//播放中

            mPlaySubscription = mRxAudioPlayer.play(PlayConfig.file(file).build())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.immediate())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            isPlaying = false;
                            info.status = AudioInfo.NORMAL;
                            SendStatusTask(info, STATUS_PLAY_COMPLETE);//播放完成

                            T nextInfo = mPlayQueue.poll();//拿出队列的值
                            if (nextInfo != null) startPlayQueue(nextInfo);//如果队列里还有没有完成的任务则继续播放
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            mPlayQueue.offer(info);//发生错误储存这个发生错误的录音
                            isPlaying = false;
                            info.status = AudioInfo.NORMAL;
                            SendStatusTask(info, STATUS_PLAY_FAILURE);//播放失败
                        }
                    });
        } else {//如果正在运行则收回之前拿出来的任务
            mPlayQueue.offer(info);
        }
    }

    /**
     * 停止播放
     */
    @Override
    public void stopPlay() {
        mRxAudioPlayer.stopPlay();

        if (mPlaySubscription != null && !mPlaySubscription.isUnsubscribed())//停止当前播放的⌚️
            mPlaySubscription.unsubscribe();
        if (mSingleSubscription != null && !mSingleSubscription.isUnsubscribed())
            mSingleSubscription.unsubscribe();

        isStop = true;
        isPlaying = false;
        if (mCurrentPlay != null) {
            mCurrentPlay.status = AudioInfo.PLAY_PAUSE;
            SendStatusTask(mCurrentPlay, STATUS_PLAY_PAUSE);//播放暂停
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void resume() {
        isStop = false;
        startPlayQueue(mCurrentPlay);//开始播放
    }


    @Override
    public void onError(@AudioRecorder.Error int error) {
        LogUtils.warnInfo(TAG, "--------------->" + error);
    }

    public static final class Builder {
        private Context mContext;
        private int mMinRecordTime = -1;
        private int mMaxRecordTime = -1;
        private AudioListener mListener;
        private StatusListener mStatusListener;
        private HttpHelper mHttpHelper;
        private Executor mExecutor = new HttpExecutor(mContext, mHttpHelper);

        private Builder() {
        }


        public Builder with(Context context) {
            if (context == null)
                throw new IllegalArgumentException("You cannot start a load on a null Context");
            this.mContext = context;
            return this;
        }

        public Builder maxRecordTime(long timeout, TimeUnit unit) {//设置后会调用监听
            if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
            if (unit == null) throw new IllegalArgumentException("unit == null");
            long seconds = unit.toSeconds(timeout);
            if (seconds > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout too large.");
            if (seconds == 0 && timeout > 0)
                throw new IllegalArgumentException("Timeout too small.");
            this.mMaxRecordTime = (int) seconds;
            return this;
        }

        public Builder minRecordTime(long timeout, TimeUnit unit) {//设置后会调用监听
            if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
            if (unit == null) throw new IllegalArgumentException("unit == null");
            long seconds = unit.toSeconds(timeout);
            if (seconds > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timeout too large.");
            if (seconds == 0 && timeout > 0)
                throw new IllegalArgumentException("Timeout too small.");
            this.mMinRecordTime = (int) seconds;
            return this;
        }


        public Builder httpHelper(HttpHelper httpHelper) {
            this.mHttpHelper = httpHelper;
            return this;
        }

        public Builder executor(Executor executor) {
            this.mExecutor = executor;
            return this;
        }

        public Builder addAudioListener(AudioListener listener) {
            this.mListener = listener;
            return this;
        }

        public Builder addStatusListener(StatusListener listener) {
            this.mStatusListener = listener;
            return this;
        }


        public AudioManager build() {
            if (mContext == null)
                throw new IllegalStateException("context is required.");
            if (mHttpHelper == null)
                throw new IllegalStateException("HttpHelper is required");

            return new AudioManager(mContext, mMaxRecordTime
                    , mMinRecordTime, mListener, mHttpHelper
                    , mStatusListener, mExecutor);
        }
    }
}
