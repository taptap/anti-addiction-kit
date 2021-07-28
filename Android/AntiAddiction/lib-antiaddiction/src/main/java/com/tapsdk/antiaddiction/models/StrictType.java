package com.tapsdk.antiaddiction.models;

import com.tapsdk.antiaddiction.annotation.IntDef;

public interface StrictType {
    int NONE = 0;
    int NIGHT = 1;
    int TIME_LIMIT = 2;

    @IntDef({NONE, NIGHT, TIME_LIMIT})
    @interface Type {

    }
}
