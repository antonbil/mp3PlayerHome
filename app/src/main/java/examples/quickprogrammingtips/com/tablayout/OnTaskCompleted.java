package examples.quickprogrammingtips.com.tablayout;
/**
 * interface to use when to call CallApi
 * necessary to create callback-functions for http-requests
 * Created by anton on 28-10-15.
 */
public interface OnTaskCompleted{
    void onTaskCompleted(String result,String call);
}