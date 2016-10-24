package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Server;

/**
 * Created by anton on 24-10-16.
 */

public class SetAndPlayOnServer {

    public SetAndPlayOnServer(Context getThis){
        showDialog(getThis);
    }

    public void showDialog(Context getThis) {
        ArrayList<Server> servers=Server.servers;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getThis);
        builderSingle.setIcon(R.drawable.common_ic_googleplayservices);
        builderSingle.setTitle("Select server to play on");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getThis,
                android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < servers.size(); i++) {
            arrayAdapter.add(servers.get(i).description);
        }

        builderSingle.setNegativeButton(
                "cancel",
                (dialog, which) -> {
                    dialog.dismiss();
                });

        builderSingle.setAdapter(
                arrayAdapter,
                (dialog, which) -> {
                    final String cat = arrayAdapter.getItem(which);
                    for (int i = 0; i < servers.size(); i++) {
                        if (servers.get(i).description.equals(cat)) {
                            int j=i;
                            setAddress(servers.get(i).url);
                            //Log.v("samba", servers.get(j).url);
                            try{
                            Server.setServer(j, SelectFragment.getThis.getActivity());
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }

                    }
                    }
                });
        builderSingle.show();
    }

    public void atFirst(){

    }
    public void atEnd(){

    }
    public void setAddress(String address) {
        new Thread(() -> {
            try {
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    Log.v("samba", "No connection with "+address);
                    if (!Logic.hasbeen)
                        Log.v("samba", "No connection2 with "+address);
                    //handler.postDelayed(() -> {
                    Toast.makeText(MainActivity.getThis, "No connection with " + Server.servers.get(Server.getServer(MainActivity.getThis)).url, Toast.LENGTH_SHORT).show();
                    //}, 2000);
                }, 400);
            } catch (Exception e){Log.getStackTraceString(e);}
            atFirst();
            setServerAddress(address);
            atEnd();
        Log.v("samba","na at end");
        }).start();
    }

    public void setServerAddress(String address) {
        MainActivity.getThis.getLogic().openServer(address);
        MainActivity.getThis.getLogic().getMpc().setMPCListener(MainActivity.getThis);
        MainActivity.getThis.playlistGetContent();
        Log.v("samba","old address:"+SpotifyFragment.ipAddress);
        SpotifyFragment.checkAddressIp(address);
        Log.v("samba","new address:"+SpotifyFragment.ipAddress);
    }

}
