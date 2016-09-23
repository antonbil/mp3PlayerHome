package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.HistoryListview;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import mpc.DatabaseCommand;
import mpc.MPCDatabaseListener;


public class DBFragment extends ListParentFragment implements MPCDatabaseListener {

    private boolean playit=false;//chdb
    private String currentId;//chdb
    private boolean download=false;

    public DBFragment(){
        super(R.id.listViewFiles2,R.layout.fragment_list2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=super.onCreateView(inflater,container,savedInstanceState);
        Thread task = new Thread()
        {
            @Override
            public void run()
            {
                displayContents(logic.getHistoryMpd().get(logic.getHistoryMpd().size() - 1).path);//chdb
            }
        };

        task.start();

        //displayContents(logic.getHistoryMpd().get(logic.getHistoryMpd().size() - 1).path);//chdb
        return view;
    }

    @Override
    public ArrayList<HistoryListview> history() {
        return logic.getHistoryMpd();
    }

    public void displayContents(String path) {
        Log.v("samba","get path "+path);
        new DatabaseCommand(logic.getMpc(),"lsinfo \""+path+"\"",this, false).run();
    }
    @Override
    public void displayContentOfDir(SambaInterface si,String path, String id) {
        if (id=="Download"){
            download=true;
            Log.v("samba","download "+path);
        }
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


        Log.v("samba","db-call succeeded");
        if (playit){
            logic.sambaCallCompleted(files1a, new ArrayList<File>(),currentId);//chdb
            playit=false;
        } else {
            if (download){
                download=false;
                ArrayList<String> downloadFiles=new ArrayList<>();
                String dirName ="-";
                for (File f:files1a) {
                    if (f instanceof Mp3File){
                        Mp3File mp=(Mp3File) f;
                        dirName = String.format("%s-%s", mp.getMpcSong().artist, mp.getMpcSong().album);
                        Log.v("samba","file contains "+mp.getMpcSong().artist+"-"+mp.getMpcSong().album);
                    }
                    String path=String.format("%s/%s","/home/wieneke/FamilyLibrary/FamilyMusic",f.getPath());
                    downloadFiles.add(path);
                    Log.v("samba", "path found:" + f.getPath());
                }
                NetworkShare.copyFile(downloadFiles, dirName);
            }else
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
