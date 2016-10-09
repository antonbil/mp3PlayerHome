package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.HistoryListview;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;


public class ListFragment extends ListParentFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=super.onCreateView(inflater,container,savedInstanceState);

        networkShare=new NetworkShare();
        //networkShare.doTest();

        displayContentOfDir(this,logic.getHistory().get(logic.getHistory().size() - 1).path, getString(R.string.select_filelist));
        return view;
    }


    @Override
    public ArrayList<HistoryListview> history() {
        return logic.getHistory();
    }
    @Override
    public void displayContentOfDir(SambaInterface si,String path, String id) {
        networkShare.getContent(si, path, id);
    }

    @Override
    public void getContentOfDirAndPlay(String path, String id) {

        if (!path.endsWith("/"))path=path+"/";
        //Log.v("samba", "path in smb:" + path);
        //todo: change next line in mpd-call to play entire dir.
        networkShare.getContent(logic, path, id);
    }

    public void back(){
        goBack(logic.getHistory());
    }
}
