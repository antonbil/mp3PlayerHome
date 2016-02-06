package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import mpc.DatabaseCommand;
import mpc.MPCDatabaseListener;


public class DBFragment extends ListParentFragment implements MPCDatabaseListener {

    private boolean playit=false;//chdb
    private String currentId;//chdb

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=super.onCreateView(inflater,container,savedInstanceState);

        displayContents(logic.getHistoryMpd().get(logic.getHistoryMpd().size() - 1));//chdb
        return view;
    }

    @Override
    public ArrayList<String> history() {
        return logic.getHistoryMpd();
    }

    public void displayContents(String path) {
        new DatabaseCommand(logic.getMpc(),"lsinfo \""+path+"\"",this, false).run();
    }
    @Override
    public void displayContentOfDir(SambaInterface si,String path, String id) {
        displayContents(path);
    }
    @Override
    public void onSaveInstanceState( Bundle outState ) {

    }
    @Override
    public void getContentOfDirAndPlay(String path, String id) {
        Log.v("samba","playa "+path);
        playit=true;
        currentId=id;
        //if path is filename
        if (path.endsWith(".mp3")){
            String[] pathLines=path.split("/");
            String f=pathLines[pathLines.length-1];
            path=path.substring(0,path.length()-f.length()-1);
            Log.v("samba","play "+path);
        }
        displayContents(path);
    }

    public void back(){
        goBack(logic.getHistoryMpd());
    }


    @Override
    public void databaseCallCompleted(final ArrayList<File> files1a) {
        for (File f:files1a)
            Log.v("samba","path found:"+f.getPath());

        if (playit){
            logic.sambaCallCompleted(files1a, new ArrayList<File>(),currentId);//chdb
            playit=false;
        } else {
            sortAndDisplay(files1a);
        }
    }

    @Override
    public void databaseUpdated() {

    }

    @Override
    public void databaseUpdateProgressChanged(int progress) {

    }

    @Override
    public void connectionFailed(String message) {

    }

}
