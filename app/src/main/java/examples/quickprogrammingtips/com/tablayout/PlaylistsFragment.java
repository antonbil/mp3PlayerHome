package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import mpc.MpcPlaylistListener;
import mpc.PlaylistsCommand;


public class PlaylistsFragment extends Fragment implements MpcPlaylistListener {
    private CopyOnWriteArrayList<String> playlists=new CopyOnWriteArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        final ListView listview = (ListView) view.findViewById(R.id.playlists_listView);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, playlists);
        listview.setAdapter(adapter);
        new PlaylistsCommand(MainActivity.getInstance().getLogic().getMpc(),"listplaylists",this).run();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                Logic logic = MainActivity.getInstance().getLogic();
                int total = logic.getPlaylistFiles().size();
                logic.getMpc().sendSingleMessage("load \"" + item + "\"");
                logic.commandWithDelay("play " + (total));
                List<Mp3File> plItems = logic.getPlaylistFiles();
                Mp3File currentItem=plItems.get(plItems.size()-1);
                currentItem.setTime(0);
                currentItem.setAlbum("");
                currentItem.setArtist("");
                currentItem.radio=true;

            }

        });

        return view;
    }
    @Override
    public void databaseCallCompleted(ArrayList<String> files) {
        List<String> subList = files.subList(1, files.size());
        Collections.sort(subList);

        for (String s:subList){
            if (!s.startsWith("00"))//todo remove create playlists in script; this is workaround...
            playlists.add(s);
        }
    }

}
