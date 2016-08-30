package me.jessyan.audiomanager.io;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import me.jessyan.audiomanager.Post;
import me.jessyan.audiomanager.entity.AudioInfo;
import me.jessyan.audiomanager.util.DeviceUtils;
import me.jessyan.audiomanager.util.LogUtils;

/**
 * Created by jess on 7/20/16.
 */
public class HttpExecutor implements Executor{
    private final String TAG = getClass().getSimpleName();
    private HttpHelper mHelper;
    private Context mContext;
    public static final int DELAY = 5_000;

    public HttpExecutor(Context context, HttpHelper mHelper) {
        this.mHelper = mHelper;
        this.mContext = context;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    execute(msg);
                    break;
            }
        }
    };

    private void execute(Message msg) {
//        final AudioInfo info = mUploadQueue.poll();
//        if (info != null) {
//            if (!DeviceUtils.netIsConnected(mContext)) {//如果没有网络则5s在执行
//                LogUtils.warnInfo(TAG, "not net--------------->");
//                mUploadQueue.offer(info);
//                mHandler.sendEmptyMessageDelayed(0, DELAY);
//                return;
//            }
//            mHelper.upload(info.filePath, new onNetWorkListener() {
//                @Override
//                public void onRequestSuccess(String url) {
//                    LogUtils.warnInfo(TAG, "success--------------->");
//                    info.url = url;
//                    mManager.sendRecordMessageToPresenter(info);
//                }
//
//                @Override
//                public void onRequestFailure() {
//                    LogUtils.warnInfo(TAG, "failure--------------->");
//                    mUploadQueue.offer(info);
//                    mHandler.sendEmptyMessage(0);
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    LogUtils.warnInfo(TAG, "error--------------->");
//                    mUploadQueue.offer(info);
//                    mHandler.sendEmptyMessage(0);
//                }
//            });
//        }


        final Bundle bundle = (Bundle) msg.obj;

        if (!DeviceUtils.netIsConnected(mContext)) {//如果没有网络则5s后在执行
            LogUtils.warnInfo(TAG, "not net--------------->");
            sendMessage(bundle, DELAY);
            return;
        }

        final AudioInfo audioInfo = (AudioInfo) bundle.getSerializable("info");
        mHelper.upload(audioInfo.filePath, new onNetWorkListener() {
            @Override
            public void onRequestSuccess(String url) {
                LogUtils.warnInfo(TAG, "success--------------->");
                Post post = (Post) bundle.getSerializable("post");
                audioInfo.url = url;
                post.sendAudioMessage(audioInfo);
            }

            @Override
            public void onRequestFailure() {
                LogUtils.warnInfo(TAG, "failure--------------->");
                sendMessage(bundle, 0);
            }

            @Override
            public void onError(Throwable throwable) {
                LogUtils.warnInfo(TAG, "error--------------->");
                sendMessage(bundle, 0);
            }
        });


    }

    public void start(AudioInfo info, Post post) {
//        mHandler.sendEmptyMessage(0);


        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);
        bundle.putSerializable("post", post);

        sendMessage(bundle, 0);
    }

    /**
     * 发送信息
     *
     * @param delay 0为不延迟
     */
    private void sendMessage(Bundle bundle, int delay) {

        Message message = mHandler.obtainMessage();
        message.what = 0;
        message.obj = bundle;
        if (delay == 0)
            mHandler.sendMessage(message);
        else
            mHandler.sendMessageDelayed(message, delay);
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
    }
}
