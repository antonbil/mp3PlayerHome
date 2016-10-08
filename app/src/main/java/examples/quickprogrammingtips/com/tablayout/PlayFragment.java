package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import mpc.DatabaseCommand;

//test line

public class PlayFragment extends Fragment implements MpdInterface {

    private PlaylistAdapter playlististAdapter;
    private Logic logic;
    PlayFragment playFragment;
    public PlayFragment(){

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure that container activity implement the callback interface
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            playFragment=this;
            // Inflate the layout for this fragment
            logic =((MainActivity)getActivity()).getLogic();
            //PlaylistThread playlistThread=new PlaylistThread(this, logic.getMpc());
            //playlistThread.run();
            View view = inflater.inflate(R.layout.fragment_list, container, false);
            final ListView playlistView = (android.widget.ListView) view.findViewById(R.id.listViewFiles);
            playlististAdapter = new PlaylistAdapter(this,this, logic.getPlaylistFiles(),getContext());
            playlistView.setAdapter(playlististAdapter);
            //registerForContextMenu(playlistView);

            ((MainActivity)getActivity()).playlistGetContent(logic.getMpc(), this);

             return view;
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 4) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // this is successful mission, do with it.
                Toast.makeText(getContext(), "succesfully returned!"+data.getExtras().getString("artist"), Toast.LENGTH_SHORT).show();
                MainActivity.getThis.tabLayout.getTabAt(2).select();
                new DatabaseCommand(logic.getMpc(),"find artist \""+data.getExtras().getString("artist")+"\"",MainActivity.getThis.dbFragment,true).run();
//                favorites.clear();
//                getFavorites();


            } else {
                // the result code is different from the one you've finished with, do something else.
            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }
    @Override
    public void playlistCall(ArrayList<Mp3File> playlist, boolean change) {
        if (!isAdded()){
            //MainActivity.panicMessage("PlayFragment is detached from Activity");
            return;
        }

        if (change){
            //final ArrayList<Mp3File> files1 = playlist;
            updateCurrentSong();
        }
    }

    public void updateCurrentSong() {
        try {
            playlististAdapter.setCurrentSong(logic.mpcStatus.song.intValue());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playlististAdapter.notifyDataSetChanged();

                }
            });
        } catch(Exception e){}
    }

    @Override
    public void newMpdCall(Mp3File mp3File,int position, String command) {
        if (!isAdded()){
            //MainActivity.panicMessage("PlayFragment is detached from Activity");
            return;
        }
        MainActivity.getThis.mpdCall(mp3File, position, command);
    }



     @Override
    public void printCover(Bitmap result,  final ImageView image,String s) {

    }


}
