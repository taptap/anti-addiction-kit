package com.taptap.tds.registration.server.dto;

import java.util.List;

public class AddictionData {

    List<InnerData> collections;

    public List<InnerData> getCollections() {
        return this.collections;
    }

    public void setCollections(List<InnerData> collections) {
        this.collections = collections;
    }
}
