package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by anton on 10-9-16.
 */
public class SpotifyPlaylistFragment extends SpotifyFragment implements HeaderSongInterface {

    private ListView tracksListview;
    public PlanetAdapter tracksAdapter;
    private ArrayList<String> albumList1 = new ArrayList<>();


    public static SpotifyPlaylistFragment getThisPlaylist;
    private ArrayList<PlaylistItem> albumTracks1 = new ArrayList<>();
    private boolean gettingList=true;
    private int previousLength=-1;
    private static Parcelable mListViewScrollPos = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the ListView position
        if (mListViewScrollPos != null) {
            tracksListview.onRestoreInstanceState(mListViewScrollPos);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the ListView position
        mListViewScrollPos = tracksListview.onSaveInstanceState();
    }

    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    public void lastOncreateView(View llview) {
        tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
        if (nextCommand.equals("search album")){
            searchAlbum();

        }else {
            if (albumList1.size()==0)
            setCurrentTracklist();
            else {
                tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
                tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);

                tracksAdapter.setDisplayCurrentTrack(true);
                tracksListview.setAdapter(tracksAdapter);
                //Log.v("samba","currentTrack:"+SpotifyFragment.currentTrack);
                tracksAdapter.setCurrentItem(SpotifyFragment.currentTrack);

            }
        }
        nextCommand="";
        //MainActivity.headers.add(this);

    }

    @Override
    public void onActivityCreated() {
        getThisPlaylist=this;
        gettingList=true;
        spotifyWorkingOnPlaylist=true;
        try {

            tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);
            tracksAdapter.setDisplayCurrentTrack(false);
            tracksListview.setAdapter(tracksAdapter);
        } catch (Exception e) {
            Log.getStackTraceString(e);
        }
    }

    public void getLayout(LayoutInflater inflater, ViewGroup container) {
        llview = inflater.inflate(R.layout.activity_spotifyplaylist, container, false);
    }

    public void setCurrentTracklist() {
        gettingList=true;
        //Log.d("samba", "Text:3a1");
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.getThis);
        MainActivity.getThis.runOnUiThread(() ->{


            //progressDialog.setCancelable(true);
            progressDialog.setMessage("Get playlist...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        });
        new Thread(() -> {
            Looper.prepare();

            try{
                SpotifyFragment.refreshPlaylistFromSpotify(1, new GetSpotifyPlaylistClass(){
                    @Override
                    public void atEnd(ArrayList<String> albumList, ArrayList<PlaylistItem> albumTracks) {
                        albumList1=albumList;
                            albumTracks1=albumTracks;
                            MainActivity.getThis.runOnUiThread(() -> {
                                try{
                                    tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
                                    tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);

                                    tracksAdapter.setDisplayCurrentTrack(true);
                                    tracksListview.setAdapter(tracksAdapter);
                                    //Log.v("samba","currentTrack:"+SpotifyFragment.currentTrack);
                                    tracksAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                                    tracksAdapter.notifyDataSetChanged();
                                    previousLength=albumList1.size();
                                    gettingList=false;
                                    spotifyWorkingOnPlaylist=false;
                                    progressDialog.dismiss();

                                }catch(Exception e){
                                    Log.v("samba", Log.getStackTraceString(e));}
                            });
                    }
                },tracksAdapter, MainActivity.getThis, albumList1, albumTracks1);

            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
        }).start();

    }

    @Override
    public void displayAlbums(){

    }

        @Override
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }

    public static PlanetAdapter getTracksAdapter(final ListView albumsListview, final ArrayList<String> albumList, final ArrayList<PlaylistItem> albumTracks) {
        PlanetAdapter albumAdapter = new SpotifyPlaylistAdapter(albumList, MainActivity.getThis, albumTracks,albumsListview) ;
        return albumAdapter;
    }

    @Override
    public void setLogo(Bitmap logo) {

    }

    @Override
    public void setData(String time, String totalTime, String title, String artist, boolean spotifyList, int currentTrack) {
        if (spotifyList)
        if (tracksAdapter!=null) {
            MainActivity.getThis.runOnUiThread(() -> {
                if ((currentTrack>=SpotifyFragment.getThis.data.albumTracks.size())||(SpotifyFragment.getThis.data.albumTracks.size()!=previousLength)){
                    if (!gettingList) {
                        //Log.v("samba","get updated list");
                        setCurrentTracklist();
                    }else
                        Log.v("samba","do not get updated list, list busy");
                }else
                try{
                    //Log.v("samba","set trak to "+currentTrack);
                    if (!gettingList) {
                        tracksAdapter.setCurrentItem(currentTrack);
                        tracksAdapter.notifyDataSetChanged();
                    }

                }catch(Exception e){
                    Log.v("samba", Log.getStackTraceString(e));}
            });
        }

    }
}
