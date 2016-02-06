package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import mpc.DatabaseCommand;

//test line

public class PlayFragment extends Fragment implements MpdInterface {

    private PlaylistAdapter playlististAdapter;
    private Logic logic;
    PlayFragment playFragment;
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
            playlististAdapter = new PlaylistAdapter(this,this, logic.getPlaylistFiles());
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
            MainActivity.panicMessage("PlayFragment is detached from Activity");
            return;
        }

        if (change){
            //final ArrayList<Mp3File> files1 = playlist;
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
    }

    @Override
    public void newMpdCall(Mp3File mp3File,int position, String command) {
        if (!isAdded()){
            MainActivity.panicMessage("PlayFragment is detached from Activity");
            return;
        }
        if (command.equals(getString(R.string.command_play)))
            logic.getMpc().play(position);
        if (command.equals(getString(R.string.playlist_removeall))){
            logic.getMpc().clearPlaylist();
        }
        if (command.equals(getString(R.string.playlist_removetop))){
            String message = "delete 0:" + (position + 1);
            Log.v("samba", message);
            enqueueSingleCommand(message);
        }
        if (command.equals(getString(R.string.playlist_removebottom))){
            String message = "delete " + (position) + ":" + (logic.getPlaylistFiles().size() + 1);
            Log.v("samba", message);
            enqueueSingleCommand(message);
            //logic.getMpc().sendSingleMessage(message);
        }
        if (command.equals(getString(R.string.playlist_removesong))){
            enqueueSingleCommand("delete " + (position));
        }
        if (command.equals(getString(R.string.playlist_movebottom))){
            enqueueSingleCommand("move " + (position) + " " + (logic.getPlaylistFiles().size() - 1));
        }
        if (command.equals(getString(R.string.playlist_down))){
            enqueueSingleCommand("move " + (position) + " " + (position + 1));
        }
        if (command.equals(getString(R.string.playlist_removeabum))){
            int top=logic.getPlaylistFiles().size()+1;
            int bottom=0;
            for (int i=0;i<logic.getPlaylistFiles().size();i++){
                if (mp3File.getAlbum().equals(logic.getPlaylistFiles().get(i).getAlbum())&&mp3File.getArtist().equals(logic.getPlaylistFiles().get(i).getArtist())){
                    if(top>i)top=i;
                    if (bottom<i)bottom=i;
                }
            }

            String message = "delete " + (top) + ":" + (bottom + 1);
            Log.v("samba", message);
            enqueueSingleCommand(message);
        }
    }

    public void enqueueSingleCommand(String message) {
        logic.getMpc().enqueCommands(new ArrayList<String>(Arrays.asList(message)));
    }

    @Override
    public void printCover(Bitmap result,  final ImageView image,String s) {

    }


}
