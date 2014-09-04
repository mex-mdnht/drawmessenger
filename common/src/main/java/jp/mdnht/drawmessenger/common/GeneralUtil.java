package jp.mdnht.drawmessenger.common;

import android.util.Log;

/**
 * Created by maeda on 2014/09/04.
 */
public class GeneralUtil {

    /**
     * As simple wrapper around Log.d
     */
    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.d(tag, message);
        }
    }
}
