package examples.quickprogrammingtips.com.tablayout.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.R;

/**
 * Created by anton on 25-1-16.
 */
public class Server {
    public int code;
    public String url;
    public String description;
    public Server(int code, String url, String description){
        this.url=url;
        this.code=code;
        this.description=description;

    }
    public static int getServer(Context context){
        final SharedPreferences app_preferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        // Get the value for the run counter
        int server = app_preferences.getInt("server", 0);
        return server;
    }
    public static void setServer(int server, Context context){
        SharedPreferences app_preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("server", server);
        editor.commit();
    }
    private static String getPref(String id){
        //String s=MainActivity.getThis.getString(id);
        try {
            return PreferenceManager.getDefaultSharedPreferences(MainActivity.getThis).getString(id, "");
        } catch (Exception e){return "";}


    }
    public static ArrayList<Server> servers=new ArrayList<>(Arrays.asList(
            new Server(R.id.selectkeuken, getPref("ipserver1"), getPref("nameserver1")),
            new Server(R.id.selectkamer, getPref("ipserver2"), getPref("nameserver2")),
            new Server(R.id.selectbank, getPref("ipserver3"), getPref("nameserver3")),
            new Server(R.id.selectstudeer, getPref("ipserver4"), getPref("nameserver4")),
    new Server(R.id.selectstudeer2, getPref("ipserver5"), getPref("nameserver5"))
            //new Server(R.id.selectxbmc, getPref("ipserver5"), getPref("nameserver5"))
    ));

}
