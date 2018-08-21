package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

import java.util.List;

/**
 *  Created by chenglei on 06/06/2017.
 */
public class PlayNumResult extends BaseEntity {

    public PlayNumResultData data;

    public class PlayNumResultData{

        private List<AdUploadResultNode> rsList;

        public List<AdUploadResultNode> getRsList() {
            return rsList;
        }

        public void setRsList(List<AdUploadResultNode> rsList) {
            this.rsList = rsList;
        }
    }

}
