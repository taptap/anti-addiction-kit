package com.tapsdk.antiaddiction.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceUtil {

    /**
     * 获取运营商信息
     */
    public static int getOperatorInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int info = 0;
        if (telephonyManager != null) {
            String NetworkOperator = telephonyManager.getNetworkOperator();
            //IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            Log.i("TdsRegionHelper", "NetworkOperator=" + NetworkOperator);
            if (NetworkOperator != null && (NetworkOperator.startsWith("460") || NetworkOperator
                    .startsWith("461"))) {
                info = 1;
            }
            return info;
        }
        return 0;
    }


}
