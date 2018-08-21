package com.bbtree.cardreader.tts;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/26
 * Time: 下午2:01
 */
public class IflytekTTSBuilder {
    private static final String TAG = IflytekTTSBuilder.class.getSimpleName();
    private static IflytekTTSBuilder ourInstance = new IflytekTTSBuilder();
    private SpeechSynthesizer mTts;
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {

        }
    };
    //合成监听器,回调都发生在主线程(UI 线程)中
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口,没有错误时,error为null
        public void onCompleted(SpeechError error) {
            if (error != null) {
                Logger.i(error.toString());
            }
        }
        //缓冲进度回调
        //percent为缓冲进度0~100, beginPos为缓冲音频在文本中开始位置, endPos表示缓冲音频在文本中结束位置,info为附加信息。

        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            Logger.i(">>>>> onBufferProgress >>>>" + percent);
        }

        //开始播放
        public void onSpeakBegin() {
            Logger.i(">>>>> onSpeakBegin >>>>");
        }

        //暂停播放
        public void onSpeakPaused() {
            Logger.i(">>>>> onSpeakPaused >>>>");
        }
        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.

        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            Logger.i(">>>>> onSpeakProgress >>>>" + percent);
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
            Logger.i(">>>>> onSpeakResumed >>>>");
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }

    };

    private IflytekTTSBuilder() {

    }

    public static IflytekTTSBuilder getInstance() {
        return ourInstance;
    }

    public IflytekTTSBuilder build(Context cxt) {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(cxt, mInitListener);
        //2.合成参数设置,详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        String voicer;
        if (DeviceConfigUtils.getConfig().getSpeaker() == Speaker.XunfeiXiaofeng) {
            voicer = "xiaofeng";
        } else {
            voicer = "xiaoyan";
        }
        int speed = DeviceConfigUtils.getConfig().getTtsSpeed();
        if (speed <= 0) {
            speed = 5;
        } else if (speed > 10) {
            speed = 10;
        }
        //选择引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        Logger.i("设置发音人资源路径:" + getResourcePath(cxt, voicer));
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);//设置发音人
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath(cxt, voicer));
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(speed * 10));//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量,范围 0~100
        mTts.setParameter(SpeechConstant.STREAM_TYPE, String.valueOf(AudioManager.STREAM_MUSIC));//音频类型
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        //设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //如果不需要保存合成音频,注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                cxt.getCacheDir().getAbsolutePath() + File.separator + "iflytek.pcm");
        return this;
    }

    public void read(String text) {
        int result = mTts.startSpeaking(text, mSynListener);
        Logger.d("-----------startSpeaking>>>" + result);
    }

    public void stop() {
        mTts.stopSpeaking();
    }

    //获取发音人资源路径
    private String getResourcePath(Context cxt, String voicer) {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(cxt, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(cxt, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + voicer + ".jet"));
        return tempBuffer.toString();
    }
}
