package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by anton on 10-9-16.
 * class to display Spotify Playlist
 */
public class SpotifyPlaylistFragment extends SpotifyFragment implements HeaderSongInterface, SpotifyPlaylistInterface {

    private ListView tracksListview;
    public static boolean refresh=true;
    public PlanetAdapter tracksAdapter;
    private ArrayList<String> albumList1 = new ArrayList<>();
    public static SpotifyPlaylistFragment getInstance() {
        return instance;
    }


    private static SpotifyPlaylistFragment instance;// automatically initialized to null
    private ArrayList<PlaylistItem> albumTracks1 = new ArrayList<>();
    public static boolean gettingList=true;
    private int previousLength=-1;
    private static Parcelable mListViewScrollPos = null;
    private ProgressDialog progressDialog;

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

            //setup playlist and adapter
            tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
            generateAdapterLists(SpotifyFragment.getData().tracksPlaylist,albumList1,albumTracks1);
            tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);

            tracksAdapter.setDisplayCurrentTrack(true);
            tracksListview.setAdapter(tracksAdapter);

            if (refresh ||(SpotifyFragment.getData().tracksPlaylist.size()==0)) {
                startDialog();
                refresh=false;
                TracksSpotifyPlaylist.getInstance().triggerPlaylist(this,40);
            }
            else
            {
                TracksSpotifyPlaylist.getInstance().triggerPlaylist(this);
            }
        }
        nextCommand="";

    }

    public void refreshSpotifyPlaylistInBackground() {
        TracksSpotifyPlaylist.getInstance().triggerPlaylist(this);
    }

    public static void generateAdapterLists(List<Track> tracksPlaylist, ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1)
    {
        albumList1.clear();
        albumTracks1.clear();
        String prevAlbum = "";
        for (Track t : tracksPlaylist) {
            final PlaylistItem pi = new PlaylistItem();
            //check for change in album-name
            String extra = "";
            try {
                String name = t.album.name;
                if (!prevAlbum.startsWith(name)) {
                    extra = String.format("(%s-%s)", t.artists.get(0).name, name);
                    prevAlbum = name;
                    pi.pictureVisible = true;
                } else
                    pi.pictureVisible = false;
            } catch (Exception e) {
                //Log.v("samba", Log.getStackTraceString(e));
            }
            //album-name can become part of title
            pi.text = t.name + extra;
            //get image-id
            //perhaps image-id already present?
            pi.url = getImageUrl(t.album.images);
            //rest of properties
            pi.id = t.id;
            pi.trackNumber = t.track_number;
            pi.time = Double.valueOf(t.duration_ms / 1000).intValue();

            albumList1.add(pi.text);
            albumTracks1.add(pi);
        }
    }
    @Override
    public void onActivityCreated() {
        if (SpotifyFragment.getData().tracksPlaylist==null)
        SpotifyFragment.getData().tracksPlaylist = new ArrayList<>();
        instance =this;
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
        startDialog();
            try{
                SpotifyFragment.refreshPlaylistFromSpotify(tracksAdapter, MainActivity.getInstance());

            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
    }

    public void startDialog() {
        if (progressDialog!=null)progressDialog.dismiss();
        progressDialog = new ProgressDialog(MainActivity.getInstance());
        MainActivity.getInstance().runOnUiThread(() ->{
            progressDialog.setMessage("Get playlist...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
            refresh=false;
        });
    }


    @Override
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }
    public static void notifyList(){
        try {
            MainActivity.getInstance().runOnUiThread(() -> {
                SpotifyPlaylistFragment.getInstance().tracksAdapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            //DebugLog.log("notify adapter error");
            //Log.v("samba", Log.getStackTraceString(e));
        }
    }
    public static PlanetAdapter getTracksAdapter(final ListView albumsListview, final ArrayList<String> albumList, final ArrayList<PlaylistItem> albumTracks) {
        return new SpotifyPlaylistAdapter(albumList, MainActivity.getInstance(), albumTracks,albumsListview);
    }

    @Override
    public void setLogo(Bitmap logo) {

    }

    @Override
    public void setData(String time, String totalTime, String title, String artist, boolean spotifyList, int currentTrack) {
        if (spotifyList) {
            if (tracksAdapter != null) {
                MainActivity.getInstance().runOnUiThread(() -> {
                    tracksAdapter.currentItem=currentTrack;
                    if ((currentTrack >= SpotifyFragment.getData().albumTracks.size()) || (SpotifyFragment.getData().albumTracks.size() != previousLength)) {
                        if (!gettingList) {
                            try {
                                refreshSpotifyPlaylistInBackground();
                            } catch (Exception e) {
                                Log.v("samba", Log.getStackTraceString(e));
                            }
                        }
                    }
                        try {
                                tracksAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }
                });
            }

        }
    }

    @Override
    public void spotifyPlaylistReturn(ArrayList<String> albumList, ArrayList<PlaylistItem> albumTracks, boolean force) {
        try {
            int max = albumTracks1.size();
            if (albumTracks.size() > max) max = albumTracks.size();
            boolean doRefresh = false;
            try {
                for (int i = 0; i < max; i++) {
                    if (!albumTracks.get(i).text.equals(albumTracks1.get(i).text)) {
                        doRefresh = true;
                        break;
                    }
                }
            } catch (Exception e) {
                doRefresh = true;
            }
            if (doRefresh||max==0)
                MainActivity.getInstance().runOnUiThread(() -> {
                    try {
                        albumTracks1.clear();
                        albumList1.clear();
                        for (int i = 0; i < albumTracks.size(); i++) {
                            PlaylistItem pi = new PlaylistItem();
                            PlaylistItem pi1 = albumTracks.get(i);
                            pi.text = pi1.text;
                            pi.id = pi1.id;
                            pi.pictureVisible = pi1.pictureVisible;
                            pi.time = pi1.time;
                            pi.trackNumber = pi1.trackNumber;
                            pi.url = pi1.url;

                            albumTracks1.add(pi);
                            albumList1.add(albumList.get(i));
                        }

                        tracksAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        DebugLog.log("error notify");
                    }

                });
            try {
                progressDialog.dismiss();
                progressDialog = null;
            } catch (Exception e) {
                //DebugLog.log("error dismass");
                //Log.v("samba", Log.getStackTraceString(e));
            }
        } catch (Exception e) {
            DebugLog.log("error refreshing");
        }
    }
}
