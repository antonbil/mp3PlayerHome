package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.os.Looper;
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
    private PlanetAdapter tracksAdapter;
    private ArrayList<String> albumList1;
    private ArrayList<PlaylistItem> albumTracks1;
    private boolean gettingList=true;
    private int previousLength=-1;

    @Override
    public void onStop(){
        MainActivity.getThis.firstTime= 0;
        //Log.v("samba","onstop");
        super.onStop();
    }
    @Override
    public void lastOncreateView(View llview) {
        tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);

        Log.d("samba", "Text:12");

        if (nextCommand.equals("search album")){
            searchAlbum();

        }else {
            //Log.d("samba", "Text:12");
            setCurrentTracklist();
            //Log.d("samba", "Text:13");
        }
        nextCommand="";
        MainActivity.headers.add(this);

    }

    @Override
    public void onActivityCreated() {
        gettingList=true;
        spotifyWorkingOnPlaylist=true;

        try {

            albumList1 = new ArrayList<>();
            albumTracks1 = new ArrayList<>();
            tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);

            tracksAdapter.setDisplayCurrentTrack(false);
            tracksListview.setAdapter(tracksAdapter);

            //Log.d("samba", "Text:6");


        } catch (Exception e) {
            Log.getStackTraceString(e);
        }
    }

    public void getLayout(LayoutInflater inflater, ViewGroup container) {
        llview = inflater.inflate(R.layout.activity_spotifyplaylist, container, false);
    }



    public void setCurrentTracklist() {
        gettingList=true;
        new Thread(() -> {
            Looper.prepare();

            try{
            //refreshPlaylistFromSpotify(albumAdapter,  MainActivity.getThis);
                SpotifyFragment.refreshPlaylistFromSpotify(1, new GetSpotifyPlaylistClass(){
                    @Override
                    public void atEnd(ArrayList<String> albumList, ArrayList<PlaylistItem> albumTracks) {
                        //Log.v("samba","atEnd");
                        for (String s:albumList){
                            //Log.v("samba","is:"+s);
                        }
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

                                }catch(Exception e){
                                    Log.v("samba", Log.getStackTraceString(e));}
                            });                            //tracksListview.setAdapter(tracksAdapter);


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
        PlanetAdapter albumAdapter = new PlanetAdapter(albumList, MainActivity.getThis, albumTracks) {
            @Override
            public void removeUp(int counter) {
                duplicateLists();

                SpotifyFragment.removeUplist(this, albumsListview, counter, MainActivity.getThis);
            }

            @Override
            public void onClickFunc(int counter) {
                try {
                    new SpotifyInterface().previousTrack.id = "";
                } catch (Exception e) {
                    //Log.v("samba", "error in starting song");
                }
                ;
                //Log.v("samba","a");
                duplicateLists();
                //Log.v("samba","b");
                SpotifyFragment.stopMpd();
                //Log.v("samba","c");
                SpotifyFragment.playlistGotoPosition(counter);
            }

            @Override
            public void removeDown(int counter) {
                duplicateLists();
                SpotifyFragment.removeDownlist(this, albumsListview, counter, MainActivity.getThis);

            }

            @Override
            public void removeAlbum(int counter) {
                duplicateLists();
                SpotifyFragment.removeAlbum(this, counter, albumsListview, MainActivity.getThis);

            }

            private void duplicateLists() {
                SpotifyFragment.getThis.data.albumList = albumList;
                SpotifyFragment.getThis.data.albumTracks = albumTracks;
            }

            @Override
            public void addAlbumToFavoritesAlbum(int counter) {

            }

            @Override
            public void addAlbumToFavoritesTrack(int counter) {
                duplicateLists();
                SpotifyFragment.addAlbumToFavoritesTrackwise(counter);

            }

            @Override
            public void removeTrack(int counter) {
                duplicateLists();
                SpotifyFragment.removeTrackSpotify(counter);

            }

            @Override
            public void displayArtist(int counter) {
                MainActivity.getThis.callSpotify(SpotifyFragment.getThis.data.tracksPlaylist.get(counter).artists.get(0).name);


            }

            @Override
            public void displayArtistWikipedia(int counter) {
                String s = SpotifyFragment.getThis.data.tracksPlaylist.get(counter).artists.get(0).name;
                MainActivity.startWikipediaPage(s);
            }

            @Override
            public void replaceAndPlayAlbum(int counter) {

            }

            @Override
            public void addAndPlayAlbum(int counter) {

            }

            @Override
            public void albumArtistWikipedia(int counter) {

            }

            @Override
            public void addAlbum(int counter) {
                SpotifyFragment.getAlbumtracksFromSpotify(SpotifyFragment.getThis.data.tracksPlaylist.get(counter).album.id, SpotifyFragment.getThis.data.tracksPlaylist.get(counter).artists.get(0).name
                        , MainActivity.getThis, this, albumsListview);

            }

            @Override
            public void addAlbumNoplay(int counter) {
            }
        };
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
                        Log.v("samba","get updated list");
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
