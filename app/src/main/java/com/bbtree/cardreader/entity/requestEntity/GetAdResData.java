package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

import java.util.List;

/**
 * Created by chenglei on 06/06/2017.
 */
public class GetAdResData extends BaseEntity {

    public Data data;

    public class Data {

        private List<Ad> ads;

        public List<Ad> getAds() {
            return ads;
        }

        public void setAds(List<Ad> ads) {
            this.ads = ads;
        }
    }
}