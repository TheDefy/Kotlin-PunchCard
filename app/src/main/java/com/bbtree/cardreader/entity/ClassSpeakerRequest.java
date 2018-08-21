package com.bbtree.cardreader.entity;

import android.text.TextUtils;
import android.util.Log;

import com.bbtree.cardreader.entity.eventbus.ClassSpeakerResponse;
import com.bbtree.childservice.utils.HexConver;

import java.util.Arrays;

import static com.bbtree.cardreader.common.Constant.ClassSpeaker.SECRETKEY1;
import static com.bbtree.cardreader.common.Constant.ClassSpeaker.SECRETKEY2;
import static com.bbtree.cardreader.entity.ClassSpeakerRequest.BroadcastRequestType.BeginHandShake;
import static com.bbtree.cardreader.entity.ClassSpeakerRequest.BroadcastRequestType.BroadcastConfigSetting;
import static com.bbtree.cardreader.entity.ClassSpeakerRequest.BroadcastRequestType.TextBroadcast;
import static com.bbtree.cardreader.entity.ClassSpeakerRequest.BroadcastRequestType.UnitTypeSetting;

/**
 * 分班播报发送命令请求与音箱交互
 */
public class ClassSpeakerRequest {

    private final String TAG = ClassSpeakerRequest.class.getSimpleName();

    /**
     * 启动握手
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     命令码
     */
    public ClassSpeakerRequest(int mDeviceType, int mCMDNum) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        this.mRequestType = BeginHandShake;
        generateCMDBytes();
        response = new ClassSpeakerResponse(mDeviceType, mCMDNum);
    }

    /**
     * 班播机配置型号
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     命令码
     * @param mPin        PIN
     * @param mGroup      组
     * @param mNumber     号
     * @param mWelcome    开机语
     */
    public ClassSpeakerRequest(int mDeviceType, int mCMDNum, int mPin,
                               int mGroup, int mNumber, String mWelcome) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        this.mPin = mPin;
        this.mGroup = mGroup;
        this.mNumber = mNumber;
        this.mWelcome = mWelcome;
        this.mRequestType = UnitTypeSetting;
        generateCMDBytes();
        response = new ClassSpeakerResponse(mDeviceType, mCMDNum, mPin, mGroup, mNumber);
    }

    /**
     * 朗读文字
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     命令码
     * @param mPin        PIN
     * @param mGroup      组
     * @param mNumber     号
     * @param mPriority   优先级
     * @param mText       朗读文字
     */
    public ClassSpeakerRequest(int mDeviceType, int mCMDNum, int mPin,
                               int mGroup, int mNumber, int mPriority, String mText) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        this.mPin = mPin;
        this.mGroup = mGroup;
        this.mNumber = mNumber;
        this.mPriority = mPriority;
        this.mText = mText;
        this.mRequestType = TextBroadcast;
        generateCMDBytes();
        response = new ClassSpeakerResponse(mDeviceType, mCMDNum, mPin, mGroup, mNumber);
    }

    /**
     * 设置播报配置（发音人语速音调等）
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     命令码
     * @param mPin        PIN
     * @param mGroup      组
     * @param mNumber     号
     * @param mSpeaker    发音人
     * @param mSpeed      语速
     * @param mTone       语调
     * @param mVolume     音量
     */
    public ClassSpeakerRequest(int mDeviceType, int mCMDNum, int mPin, int mGroup, int mNumber,
                               int mSpeaker, int mSpeed, int mTone, int mVolume) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        this.mPin = mPin;
        this.mGroup = mGroup;
        this.mNumber = mNumber;
        this.mSpeaker = mSpeaker;
        this.mSpeed = mSpeed;
        this.mTone = mTone;
        this.mVolume = mVolume;
        this.mRequestType = BroadcastConfigSetting;
        generateCMDBytes();
        response = new ClassSpeakerResponse(mDeviceType, mCMDNum, mPin, mGroup, mNumber);
    }

    private void generateCMDBytes() {
        checkValues();
        // 暂存PIN转16进制字符串
        StringBuilder tempPin;
        // 暂存welcome或text的内容转16进制的字符串
        StringBuilder tempText = new StringBuilder();
        // 暂存welcome或text的单个字节内容转16进制的字符串
        StringBuilder hexB;
        // 暂存welcome或text的内容转16进制的字符串的长度
        StringBuilder tempTextLength;
        // 暂存welcome或text的内容转16进制的和
        int tempSum = 0;
        switch (mRequestType) {
            case BeginHandShake:
                hexCMD = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + mFooter;
                checkCode = Integer.toHexString(mDeviceType + mCMDNum + SECRETKEY1 + SECRETKEY2);
                break;
            case UnitTypeSetting:
                // 计算PIN和PIN长度
                tempPin = new StringBuilder(Integer.toHexString(mPin));
                while (tempPin.length() < 4) {
                    tempPin.insert(0, "0");
                }
                mPinLenght = tempPin.length() / 2;
                // 计算welcome语和welcome语长度
                for (int i = 0; i < mWelcome.length(); i++) {
                    char charAt = mWelcome.charAt(i);
                    hexB = new StringBuilder(Integer.toHexString(charAt));
                    if (hexB.length() <= 2) {
                        hexB.insert(0, "00");
                    }
                    String hexB1 = hexB.substring(0, 2);
                    String hexB2 = hexB.substring(2);
                    String tempHexB = hexB2 + hexB1;
                    tempText.append(tempHexB);
                    tempSum += Integer.parseInt(hexB1, 16);
                    tempSum += Integer.parseInt(hexB2, 16);
                }
                mWelcomeLength = tempText.length() / 2;
                tempTextLength = new StringBuilder(Integer.toHexString(mWelcomeLength));
                while (tempTextLength.length() < 4) {
                    tempTextLength.insert(0, "0");
                }
                hexCMD = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
                        + tempPin
                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
                        + tempTextLength
                        + tempText
                        + mFooter;
                checkCode = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght
                        + Integer.parseInt(tempPin.substring(0, 2), 16)
                        + Integer.parseInt(tempPin.substring(2), 16)
                        + mGroup + mNumber + mWelcomeLength + tempSum + SECRETKEY1 + SECRETKEY2);
                break;
            case TextBroadcast:
                // 计算PIN和PIN长度
                tempPin = new StringBuilder(Integer.toHexString(mPin));
                while (tempPin.length() < 4) {
                    tempPin.insert(0, "0");
                }
                mPinLenght = tempPin.length() / 2;
                // 计算welcome语和welcome语长度
                for (int i = 0; i < mText.length(); i++) {
                    char charAt = mText.charAt(i);
                    hexB = new StringBuilder(Integer.toHexString(charAt));
                    if (hexB.length() <= 2) {
                        hexB.insert(0, "00");
                    }
                    String hexB1 = hexB.substring(0, 2);
                    String hexB2 = hexB.substring(2);
                    String tempHexB = hexB2 + hexB1;
                    tempText.append(tempHexB);
                    tempSum += Integer.parseInt(hexB1, 16);
                    tempSum += Integer.parseInt(hexB2, 16);
                }
                mTextLength = tempText.length() / 2;
                tempTextLength = new StringBuilder(Integer.toHexString(mTextLength));
                while (tempTextLength.length() < 4) {
                    tempTextLength.insert(0, "0");
                }
                hexCMD = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
                        + tempPin
                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
                        + (mPriority < 16 ? Integer.toHexString(0) + Integer.toHexString(mPriority) : Integer.toHexString(mPriority))
                        + tempTextLength
                        + tempText
                        + mFooter;
                checkCode = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght
                        + Integer.parseInt(tempPin.substring(0, 2), 16)
                        + Integer.parseInt(tempPin.substring(2), 16)
                        + mGroup + mNumber + mPriority + mTextLength + tempSum + SECRETKEY1 + SECRETKEY2);
                break;
            case BroadcastConfigSetting:
                // 计算PIN和PIN长度
                tempPin = new StringBuilder(Integer.toHexString(mPin));
                while (tempPin.length() < 4) {
                    tempPin.insert(0, "0");
                }
                mPinLenght = tempPin.length() / 2;
                hexCMD = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
                        + tempPin
                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
                        + (mSpeaker < 16 ? Integer.toHexString(0) + Integer.toHexString(mSpeaker) : Integer.toHexString(mSpeaker))
                        + (mSpeed < 16 ? Integer.toHexString(0) + Integer.toHexString(mSpeed) : Integer.toHexString(mSpeed))
                        + (mTone < 16 ? Integer.toHexString(0) + Integer.toHexString(mTone) : Integer.toHexString(mTone))
                        + (mVolume < 16 ? Integer.toHexString(0) + Integer.toHexString(mVolume) : Integer.toHexString(mVolume))
                        + mFooter;
                checkCode = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght + mPin + mGroup + mNumber + mSpeaker + mSpeed + mTone + mVolume + SECRETKEY1 + SECRETKEY2);
                break;
//            case GroupBroadcast:
//                hexCMD = mHeader
//                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
//                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
//                        + mFooter;
//                checkCode = Integer.toHexString(mDeviceType + mCMDNum + SECRETKEY1 + SECRETKEY2).toUpperCase();
//                break;
//            case WelcomeSetting:
//                tempText = HexConver.str2HexStr(mText).replace(" ", "");
//                mTextLength = tempText.length() / 2;
//                if (mTextLength < 16) {
//                    tempLength = Integer.toHexString(0) + Integer.toHexString(0) + Integer.toHexString(0) + Integer.toHexString(mTextLength);
//                } else if (mTextLength < 256) {
//                    tempLength = Integer.toHexString(0) + Integer.toHexString(0) + Integer.toHexString(mTextLength);
//                } else if (mTextLength < 4096) {
//                    tempLength = Integer.toHexString(0) + Integer.toHexString(mTextLength);
//                } else if (mTextLength < 65536) {
//                    tempLength = Integer.toHexString(mTextLength);
//                }
//                hexCMD = mHeader
//                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
//                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
//                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
//                        + (mPin < 16 ? Integer.toHexString(0) + Integer.toHexString(mPin) : Integer.toHexString(mPin))
//                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
//                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
//                        + tempLength
//                        + HexConver.str2HexStr(mText)
//                        + mFooter;
//                tempSplit = HexConver.str2HexStr(mText).split(" ");
//                tempSum = 0;
//                for (String tempS : tempSplit) {
//                    tempSum += Integer.parseInt(tempS, 16);
//                }
//                checkCode = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght + mPin + mGroup + mNumber + mTextLength + SECRETKEY1 + SECRETKEY2) + tempSum;
//                break;
        }
        if (TextUtils.isEmpty(hexCMD) || TextUtils.isEmpty(checkCode)) {
            return;
        }
        if (checkCode.length() > 4) {
            checkCode = checkCode.substring(checkCode.length() - 4);
            String header = checkCode.substring(0, 2);
            String footer = checkCode.substring(2);
            checkCode = footer + header;
        } else {
            if (checkCode.length() == 4) {
                String header = checkCode.substring(0, 2);
                String footer = checkCode.substring(2);
                checkCode = footer + header;
            } else if (checkCode.length() == 3) {
                String header = checkCode.substring(0, 1);
                String footer = checkCode.substring(1);
                checkCode = footer + "0" + header;
            } else if (checkCode.length() == 2) {
                checkCode = checkCode + "00";
            } else {
                checkCode = checkCode + "000";
            }
        }
        hexCMD = hexCMD.toUpperCase();
        hexCMD = hexCMD.replace(" ", "");
        checkCode = checkCode.toUpperCase();
//        Log.e(TAG, "hexCMD:" + hexCMD);
//        Log.e(TAG, "checkCode:" + checkCode);
        mCMDBytes = HexConver.hexStr2Bytes(hexCMD + checkCode);
    }

    /**
     * 检验状态
     */
    private void checkValues() {
        if (mDeviceType > 255) {
            Log.e(TAG, "mDeviceType too large:" + mDeviceType);
            throw new RuntimeException("BeginHandShake: mDeviceType too large:" + mDeviceType);
        }
        if (mCMDNum > 255) {
            Log.e(TAG, "mCMDNum too large:" + mCMDNum);
            throw new RuntimeException("BeginHandShake: mDeviceType too large:" + mDeviceType);
        }
        if (mPin > 65535) {
            Log.e(TAG, "mPin too large:" + mPin);
            throw new RuntimeException("BeginHandShake: mPin too large:" + mPin);
        }
        if (mGroup > 255) {
            Log.e(TAG, "mGroup too large:" + mGroup);
            throw new RuntimeException("BeginHandShake: mDeviceType too large:" + mDeviceType);
        }
        if (mNumber > 255) {
            Log.e(TAG, "mNumber too large:" + mNumber);
            throw new RuntimeException("BeginHandShake: mDeviceType too large:" + mDeviceType);
        }
    }

    /**
     * 帧头
     */
    private String mHeader = HexConver.str2HexStr("{");

    /**
     * 设备型号
     */
    private int mDeviceType;

    /**
     * 命令码
     */
    private int mCMDNum;

    /**
     * PIN长度
     */
    private int mPinLenght;

    /**
     * PIN
     */
    private int mPin;

    /**
     * 组
     */
    private int mGroup;

    /**
     * 号
     */
    private int mNumber;

    /**
     * 发音人
     */
    private int mSpeaker;

    /**
     * 语速
     */
    private int mSpeed;

    /**
     * 语调
     */
    private int mTone;

    /**
     * 音量
     */
    private int mVolume;

    /**
     * 开机语长度
     */
    private int mWelcomeLength;

    /**
     * 开机语
     */
    private String mWelcome;

    /**
     * 文字长度
     */
    private int mTextLength;

    /**
     * 文字内容
     */
    private String mText;

    /**
     * 抢音:0抢音,1为正常排队,2插队
     */
    private int mPriority;

    /**
     * 帧尾
     */
    private String mFooter = HexConver.str2HexStr("}");

    private BroadcastRequestType mRequestType;

    /**
     * 要发送的hex
     */
    private String hexCMD;

    /**
     * 校验码
     */
    private String checkCode;

    /**
     * Hex指令集
     */
    private byte[] mCMDBytes;

    private ClassSpeakerResponse response;

    public String getHexCMD() {
        return hexCMD;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public byte[] getmCMDBytes() {
        return mCMDBytes;
    }

    public ClassSpeakerResponse getResponse() {
        return response;
    }


    /**
     * 请求消息类型
     */
    public enum BroadcastRequestType {
        /**
         * 启动握手
         */
        BeginHandShake,
        /**
         * 班播机配置型号
         */
        UnitTypeSetting,
        /**
         * 朗读文字
         */
        TextBroadcast,
        /**
         * 设置播报配置
         */
        BroadcastConfigSetting,
        /**
         * 集体播报
         */
        GroupBroadcast,
        /**
         * 配置开机语
         */
        WelcomeSetting
    }

    @Override
    public String toString() {
        return "ClassSpeakerRequest{" +
                "TAG='" + TAG + '\'' +
                ", mHeader='" + mHeader + '\'' +
                ", mDeviceType=" + mDeviceType +
                ", mCMDNum=" + mCMDNum +
                ", mPinLenght=" + mPinLenght +
                ", mPin=" + mPin +
                ", mGroup=" + mGroup +
                ", mNumber=" + mNumber +
                ", mSpeaker=" + mSpeaker +
                ", mSpeed=" + mSpeed +
                ", mTone=" + mTone +
                ", mVolume=" + mVolume +
                ", mWelcomeLength=" + mWelcomeLength +
                ", mWelcome='" + mWelcome + '\'' +
                ", mTextLength=" + mTextLength +
                ", mText='" + mText + '\'' +
                ", mPriority=" + mPriority +
                ", mFooter='" + mFooter + '\'' +
                ", mRequestType=" + mRequestType +
                ", hexCMD='" + hexCMD + '\'' +
                ", checkCode='" + checkCode + '\'' +
                ", mCMDBytes=" + Arrays.toString(mCMDBytes) +
                ", response=" + response +
                '}';
    }
}
