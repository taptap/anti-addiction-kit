package com.taptap.tds.registration.server.core.domain;

import java.io.Serializable;


public class PageQuery implements Serializable {

    private static final long serialVersionUID = 3173070192633716255L;

    private int skip = 0;

    private int limit = -1;

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
