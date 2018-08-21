package defy.com.punchcard;

import java.io.Serializable;

/**
 * Created by chenglei on 2017/9/21.
 */

public class CardPunchRecord implements Serializable {
    private String card_holder;
    private String cardNo;
    private String macId;
    private String imgUrl;
    private boolean has_sync;
    private boolean has_upload;
    private String recordId;
    private String photoByte;
    private long punchTime;

    public String getCard_holder() {
        return card_holder;
    }

    public void setCard_holder(String card_holder) {
        this.card_holder = card_holder;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isHas_sync() {
        return has_sync;
    }

    public void setHas_sync(boolean has_sync) {
        this.has_sync = has_sync;
    }

    public boolean isHas_upload() {
        return has_upload;
    }

    public void setHas_upload(boolean has_upload) {
        this.has_upload = has_upload;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPhotoByte() {
        return photoByte;
    }

    public void setPhotoByte(String photoByte) {
        this.photoByte = photoByte;
    }

    public long getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(long punchTime) {
        this.punchTime = punchTime;
    }
}
