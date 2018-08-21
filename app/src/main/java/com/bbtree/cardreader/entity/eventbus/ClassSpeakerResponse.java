package com.bbtree.cardreader.entity.eventbus;

import android.text.TextUtils;

import com.bbtree.childservice.utils.HexConver;

import static com.bbtree.cardreader.common.Constant.ClassSpeaker.SECRETKEY1;
import static com.bbtree.cardreader.common.Constant.ClassSpeaker.SECRETKEY2;

/**
 * 分班播报应答命令消息-音箱应答
 */
public class ClassSpeakerResponse {

    /**
     * 启动握手应答
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     源码命令
     */
    public ClassSpeakerResponse(int mDeviceType, int mCMDNum) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        mResponseType = ClassSpeakerResponseType.BEGIN_HAND_SHAKE;
        generateCMDResultBytes();
    }

    /**
     * 班播机配置型号应答
     *
     * @param mDeviceType 设备型号
     * @param mCMDNum     源码命令
     * @param mPin        PIN
     * @param mGroup      组
     * @param mNumber     号
     */
    public ClassSpeakerResponse(int mDeviceType, int mCMDNum, int mPin, int mGroup, int mNumber) {
        this.mDeviceType = mDeviceType;
        this.mCMDNum = mCMDNum;
        this.mPin = mPin;
        this.mGroup = mGroup;
        this.mNumber = mNumber;
        this.mResponseType = ClassSpeakerResponseType.UNIT_TYPE_SETTING;
        generateCMDResultBytes();
    }

    private void generateCMDResultBytes() {
        int mAnswerCodeOKInt = Integer.parseInt(mAnswerCodeOK, 16);
        int mAnswerCodeFailInt = Integer.parseInt(mAnswerCodeFail, 16);
        switch (mResponseType) {
            case BEGIN_HAND_SHAKE:
                hexCMDResult = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + mAnswerCodeOK
                        + mFooter;
                checkCodeResult = Integer.toHexString(mDeviceType + mCMDNum
                        + mAnswerCodeOKInt + SECRETKEY1 + SECRETKEY2);
                break;
            case BROADCAST_CONFIG_SETTING:
            case UNIT_TYPE_SETTING:
//            case GroupBroadcast:
            case WELCOME_SETTING:
            case TEXT_BROADCAST:
                // 计算PIN和PIN长度
                // 暂存PIN转16进制字符串
                StringBuilder tempPin = new StringBuilder(Integer.toHexString(mPin));
                while (tempPin.length() < 4) {
                    tempPin.insert(0, "0");
                }
                mPinLenght = tempPin.length() / 2;
                hexCMDResult = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
                        + tempPin
                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
                        + mAnswerCodeOK
                        + mFooter;
                hexCMDResultFail = mHeader
                        + (mDeviceType < 16 ? Integer.toHexString(0) + Integer.toHexString(mDeviceType) : Integer.toHexString(mDeviceType))
                        + (mCMDNum < 16 ? Integer.toHexString(0) + Integer.toHexString(mCMDNum) : Integer.toHexString(mCMDNum))
                        + (mPinLenght < 16 ? Integer.toHexString(0) + Integer.toHexString(mPinLenght) : Integer.toHexString(mPinLenght))
                        + tempPin
                        + (mGroup < 16 ? Integer.toHexString(0) + Integer.toHexString(mGroup) : Integer.toHexString(mGroup))
                        + (mNumber < 16 ? Integer.toHexString(0) + Integer.toHexString(mNumber) : Integer.toHexString(mNumber))
                        + mAnswerCodeFail
                        + mFooter;

                checkCodeResult = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght
                        + Integer.parseInt(tempPin.substring(0, 2), 16)
                        + Integer.parseInt(tempPin.substring(2), 16)
                        + mGroup + mNumber + mAnswerCodeOKInt + SECRETKEY1 + SECRETKEY2);
                checkCodeResultFail = Integer.toHexString(mDeviceType + mCMDNum + mPinLenght
                        + Integer.parseInt(tempPin.substring(0, 2), 16)
                        + Integer.parseInt(tempPin.substring(2), 16)
                        + mGroup + mNumber + mAnswerCodeFailInt + SECRETKEY1 + SECRETKEY2);
                break;
        }
        if (TextUtils.isEmpty(hexCMDResult) || TextUtils.isEmpty(checkCodeResult)) {
            return;
        }
        if (checkCodeResult.length() > 4) {
            checkCodeResult = checkCodeResult.substring(checkCodeResult.length() - 4);
            String header = checkCodeResult.substring(0, 2);
            String footer = checkCodeResult.substring(2);
            checkCodeResult = footer + header;
        } else {
            if (checkCodeResult.length() == 4) {
                String header = checkCodeResult.substring(0, 2);
                String footer = checkCodeResult.substring(2);
                checkCodeResult = footer + header;
            } else if (checkCodeResult.length() == 3) {
                String header = checkCodeResult.substring(0, 1);
                String footer = checkCodeResult.substring(1);
                checkCodeResult = footer + "0" + header;
            } else if (checkCodeResult.length() == 2) {
                checkCodeResult = checkCodeResult + "00";
            } else {
                checkCodeResult = checkCodeResult + "000";
            }
        }
        hexCMDResult = hexCMDResult.toUpperCase();
        hexCMDResult = hexCMDResult.replace(" ", "");

        checkCodeResultFail = checkCodeResultFail.toUpperCase();
        if (checkCodeResultFail.length() > 4) {
            checkCodeResultFail = checkCodeResultFail.substring(checkCodeResultFail.length() - 4);
            String header = checkCodeResultFail.substring(0, 2);
            String footer = checkCodeResultFail.substring(2);
            checkCodeResultFail = footer + header;
        } else {
            if (checkCodeResultFail.length() == 4) {
                String header = checkCodeResultFail.substring(0, 2);
                String footer = checkCodeResultFail.substring(2);
                checkCodeResultFail = footer + header;
            } else if (checkCodeResultFail.length() == 3) {
                String header = checkCodeResultFail.substring(0, 1);
                String footer = checkCodeResultFail.substring(1);
                checkCodeResultFail = footer + "0" + header;
            } else if (checkCodeResultFail.length() == 2) {
                checkCodeResultFail = checkCodeResultFail + "00";
            } else {
                checkCodeResultFail = checkCodeResultFail + "000";
            }
        }

        hexCMDResult = hexCMDResult.toUpperCase();
        hexCMDResult = hexCMDResult.replace(" ", "");
        checkCodeResult = checkCodeResult.toUpperCase();
        ResultCMD = hexCMDResult + checkCodeResult;
//        Log.e("ClassSpeakerResponse", "hexCMDResult:" + hexCMDResult);
//        Log.e("ClassSpeakerResponse", "checkCodeResult:" + checkCodeResult);

        hexCMDResultFail = hexCMDResultFail.toUpperCase();
        hexCMDResultFail = hexCMDResultFail.replace(" ", "");
        checkCodeResultFail = checkCodeResultFail.toUpperCase();
        ResultCMDFail = hexCMDResultFail + checkCodeResultFail;
//        Log.e("ClassSpeakerResponse", "hexCMDResultFail:" + hexCMDResultFail);
//        Log.e("ClassSpeakerResponse", "checkCodeResultFail:" + checkCodeResultFail);
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
     * 源命令码
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
     * 应答码:成功
     */
    private String mAnswerCodeOK = HexConver.str2HexStr("Y");

    /**
     * 应答码:失败
     */
    private String mAnswerCodeFail = HexConver.str2HexStr("N");

    /**
     * 帧尾
     */
    private String mFooter = HexConver.str2HexStr("}");

    /**
     * 正确的hex回应码
     */
    private String hexCMDResult = "";

    /**
     * 失败的hex回应码
     */
    private String hexCMDResultFail = "";

    /**
     * 正确的hex回应码对应的校验码
     */
    private String checkCodeResult = "";

    /**
     * 失败的hex回应码对应的校验码
     */
    private String checkCodeResultFail = "";

    /**
     * 正确的hex回应(回应码+校验码)
     */
    private String ResultCMD = "";

    /**
     * 失败的hex回应(回应码+校验码)
     */
    private String ResultCMDFail = "";

    public String getResultCMD() {
        return ResultCMD;
    }

    public String getHexCMDResult() {
        return hexCMDResult;
    }

    public String getResultCMDFail() {
        return ResultCMDFail;
    }

    public String getCheckCodeResult() {
        return checkCodeResult;
    }

    public String getCheckCodeResultFail() {
        return checkCodeResultFail;
    }

    private ClassSpeakerResponse.ClassSpeakerResponseType mResponseType;

    /**
     * 请求消息类型
     */
    public enum ClassSpeakerResponseType {
        /**
         * 启动握手
         */
        BEGIN_HAND_SHAKE,
        /**
         * 设置播报配置
         */
        BROADCAST_CONFIG_SETTING,
        /**
         * 班播机配置型号
         */
        UNIT_TYPE_SETTING,
//        /**
//         * 集体播报
//         */
//        GroupBroadcast,
        /**
         * 配置开机语
         */
        WELCOME_SETTING,
        /**
         * 朗读文字
         */
        TEXT_BROADCAST
    }

}
