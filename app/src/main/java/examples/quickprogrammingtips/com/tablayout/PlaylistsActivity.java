package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import mpc.DatabaseCommand;
import mpc.MpcPlaylistListener;
import mpc.PlaylistsCommand;

public class PlaylistsActivity extends AppCompatActivity implements MpcPlaylistListener {

    private CopyOnWriteArrayList<String> playlists=new CopyOnWriteArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ListView listview = (ListView) findViewById(R.id.playlists_listView);
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, playlists);
        listview.setAdapter(adapter);
        new PlaylistsCommand(MainActivity.getThis.getLogic().getMpc(),"listplaylists",this).run();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //finish the current activity
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                Log.v("samba", "selected:" + item);//http://icecast.omroep.nl/radio4-bb-mp3
                Logic logic = MainActivity.getThis.getLogic();
                int total= logic.getPlaylistFiles().size();
                logic.getMpc().sendSingleMessage("load \""+item+"\"");
                logic.commandWithDelay("play "+(total));

                view.animate().setDuration(2000).alpha(new Float(0.5))
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
            }

        });
    }

    @Override
    public void databaseCallCompleted(ArrayList<String> files) {
        //this.playlists=files;
        List<String> subList = files.subList(1, files.size());
        Collections.sort(subList);

        for (String s:subList){
            playlists.add(s);
            Log.v("samba",s);
        }
    }
}
