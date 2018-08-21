package com.bbtree.cardreader.tts;

import android.content.Context;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/27
 * Time: 下午3:51
 */
public class BaiduTTSBuilder {
    private static final String TAG = BaiduTTSBuilder.class.getSimpleName();
    private static BaiduTTSBuilder ourInstance = new BaiduTTSBuilder();
    private Context mContext;

    private BaiduTTSBuilder() {

    }

    public static BaiduTTSBuilder getInstance() {
        return ourInstance;
    }

    public void build(Context cxt) {
        mContext = cxt;
        initialEnv();
        initialTts();
    }

    private SpeechSynthesizer mSpeechSynthesizer;
    private String mTTSDirPath;
    private static final String BaiduTTS_DIR_NAME = "BaiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = mContext.getResources().getAssets().open("tts_baidu/" + source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initialEnv() {
        if (mTTSDirPath == null) {
            String sdcardPath = null;
            if (mContext.getExternalCacheDir() != null) {
                sdcardPath = mContext.getExternalCacheDir().getAbsolutePath();
            } else {
                sdcardPath = mContext.getCacheDir().getAbsolutePath();
            }

            mTTSDirPath = sdcardPath + "/" + BaiduTTS_DIR_NAME;
        }
        makeDir(mTTSDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mTTSDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mTTSDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mTTSDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mTTSDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mTTSDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mTTSDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private SpeechSynthesizerListener mSpeechSynthesizerListener = new SpeechSynthesizerListener() {

        @Override
        public void onSynthesizeStart(String s) {

        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

        }

        @Override
        public void onSynthesizeFinish(String s) {

        }

        @Override
        public void onSpeechStart(String s) {

        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {

        }

        @Override
        public void onSpeechFinish(String s) {

        }

        @Override
        public void onError(String s, SpeechError speechError) {

        }

    };

    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(mContext);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(mSpeechSynthesizerListener);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mTTSDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mTTSDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);

        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("6066592");
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("YqG4LBE0lHHXkMb6b52EsN6b", "c5ebd0ea0f5816138bec7ecea53aab29");
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        if (DeviceConfigUtils.getConfig().getSpeaker() == Speaker.BaiduMale) {
            this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "1");
        } else {
            this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        }
        int speed = DeviceConfigUtils.getConfig().getTtsSpeed();
        if (speed >= 10) {
            speed = 9;
        } else if (speed <= 0) {
            speed = 5;
        }
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, String.valueOf(speed));//范围[0-9]
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE, "AUDIO_ENCODE_AMR");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOCODER_OPTIM_LEVEL, "2");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
//        // 授权检测接口(可以不使用，只是验证授权是否成功)speechSynthesizer
//        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
//        if (authInfo.isSuccess()) {
//            LOG.t(TAG).d("auth success");
//        } else {
//            String errorMsg = authInfo.getTtsError().getDetailMessage();
//            LOG.t(TAG).d("auth failed errorMsg=" + errorMsg);
//        }
        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mTTSDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mTTSDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        Logger.d("loadEnglishModel result=" + result);
    }

    public void read(String text) {
        mSpeechSynthesizer.stop();
        mSpeechSynthesizer.speak(text);
    }

}
