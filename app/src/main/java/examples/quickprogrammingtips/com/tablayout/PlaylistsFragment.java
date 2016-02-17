package examples.quickprogrammingtips.com.tablayout;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Logic;
import mpc.MpcPlaylistListener;
import mpc.PlaylistsCommand;


public class PlaylistsFragment extends Fragment implements MpcPlaylistListener {
    private CopyOnWriteArrayList<String> playlists=new CopyOnWriteArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View view=super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        final ListView listview = (ListView) view.findViewById(R.id.playlists_listView);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, playlists);
        listview.setAdapter(adapter);
        new PlaylistsCommand(MainActivity.getThis.getLogic().getMpc(),"listplaylists",this).run();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                Log.v("samba", "selected:" + item);//http://icecast.omroep.nl/radio4-bb-mp3
                Logic logic = MainActivity.getThis.getLogic();
                int total = logic.getPlaylistFiles().size();
                logic.getMpc().sendSingleMessage("load \"" + item + "\"");
                logic.commandWithDelay("play " + (total));

                view.animate().setDuration(2000).alpha(new Float(0.5))
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
            }

        });

        return view;
    }
    @Override
    public void databaseCallCompleted(ArrayList<String> files) {
        //this.playlists=files;
        List<String> subList = files.subList(1, files.size());
        Collections.sort(subList);

        for (String s:subList){
            if (!s.startsWith("00"))
            playlists.add(s);
            Log.v("samba",s);
        }
    }

}
