package me.jessyan.audiomanager;

/**
 * Created by jess on 7/16/16.
 */
public interface AudioListener {
    /**
     * 录制时间太短
     */
    void onRecordTooShort();

    /**
     * 录制时间太长
     */
    void onRecordTooLong();

    /**
     * 录制时声音的振奋登记
     */
    void onVoiceAmplitudeLevel(int level);
}

