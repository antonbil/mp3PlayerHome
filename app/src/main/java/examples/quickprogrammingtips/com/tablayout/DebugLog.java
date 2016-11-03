package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 26-10-16.
 * helper class to display debug-messages with row-number
 */

import android.util.Log;
public class DebugLog {
    private final static boolean DEBUG = true;
    public static void log(String message) {
        if (DEBUG) {
            String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

            Log.v("samba",className + "." + methodName + "():" + lineNumber+":"+message);
        }
    }
}
