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
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by anton on 10-9-16.
 */
public class SpotifyPlaylistFragment extends SpotifyFragment implements HeaderSongInterface {

    private ListView tracksListview;
    public static boolean refresh=true;
    public PlanetAdapter tracksAdapter;
    private ArrayList<String> albumList1 = new ArrayList<>();


    public static SpotifyPlaylistFragment getThisPlaylist;
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
            if (refresh ||(SpotifyFragment.data.tracksPlaylist.size()==0)) {
                //DebugLog.log("new list!");
                setCurrentTracklist();
            }
            else
            {

                refreshSpotifyPlaylistInBackground();
                tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
                generateAdapterLists(SpotifyFragment.data.tracksPlaylist,albumList1,albumTracks1);
                tracksAdapter = getTracksAdapter(tracksListview, albumList1, albumTracks1);

                tracksAdapter.setDisplayCurrentTrack(true);
                tracksListview.setAdapter(tracksAdapter);

                tracksAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                tracksAdapter.notifyDataSetChanged();
            }
            refresh=false;
        }
        nextCommand="";

    }

    public void refreshSpotifyPlaylistInBackground() {
        new Thread(() -> {
            refresh=false;
            Looper.prepare();
            MainActivity.getThis.leftDrawerPlaylist.getDrawerSpotifyPlaylist(new GetSpotifyPlaylistClass(){
                @Override
                public void atEnd(ArrayList<String> albumList, ArrayList<PlaylistItem> albumTracks) {
                    //DebugLog.log("start refresh");
                    //DebugLog.log("st");

                    int max=albumTracks1.size();
                    if (albumTracks.size()>max)max=albumTracks.size();
                    boolean doRefresh=false;
                    try {
                        for (int i = 0; i < max; i++) {
                            //DebugLog.log("st");
                            if (!albumTracks.get(i).text.equals(albumTracks1.get(i).text)) {
                                doRefresh = true;
                                break;
                            }
                        }
                    }catch(Exception e){doRefresh = true;}
                            //DebugLog.log("atend");
                    if (doRefresh)
                        activityThis.runOnUiThread(() -> {
                            DebugLog.log("now refresh!");
                            albumTracks1.clear();
                            albumList1.clear();
                            for (int i=0;i<albumTracks.size();i++){
                                PlaylistItem pi=new PlaylistItem();
                                PlaylistItem pi1=albumTracks.get(i);
                                pi.text=pi1.text;
                                pi.id=pi1.id;
                                pi.pictureVisible=pi1.pictureVisible;
                                pi.time=pi1.time;
                                pi.trackNumber=pi1.trackNumber;
                                pi.url=pi1.url;

                                //DebugLog.log(albumTracks.get(i).text);
                                albumTracks1.add(pi);
                                albumList1.add(albumList.get(i));
                            }
                            tracksAdapter.notifyDataSetChanged();
                        });
                }
            });
        }).start();
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
                Log.v("samba", Log.getStackTraceString(e));
            }
            //album-name can become part of title
            pi.text = t.name + extra;
            //get image-id
            new DownLoadImageUrlTask() {
                @Override
                public void setUrl(String logo) {
                    pi.url = logo;
                }
            }.execute(t.album.id);
            //perhaps image-id already present?
            pi.url = getImageUrl(t.album.images);
            //rest of properties
            pi.id = t.id;
            pi.trackNumber = t.track_number;
            int time = new Double(t.duration_ms / 1000).intValue();
            pi.time = time;

            albumList1.add(pi.text);
            albumTracks1.add(pi);

        }
    }
    @Override
    public void onActivityCreated() {
        if (SpotifyFragment.getThis.data.tracksPlaylist==null)
        SpotifyFragment.getThis.data.tracksPlaylist = new ArrayList<Track>();
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
        if (progressDialog!=null)progressDialog.dismiss();
        progressDialog = new ProgressDialog(MainActivity.getThis);
        MainActivity.getThis.runOnUiThread(() ->{


            //progressDialog.setCancelable(true);
            progressDialog.setMessage("Get playlist...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
            refresh=false;
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
                                    //DebugLog.log("currentTrack:"+SpotifyFragment.currentTrack);
                                    tracksAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                                    tracksAdapter.notifyDataSetChanged();
                                    previousLength=albumList1.size();
                                    gettingList=false;
                                    spotifyWorkingOnPlaylist=false;
                                    progressDialog.dismiss();
                                    progressDialog=null;

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
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }
    public static void notifyList(){
        try{
        MainActivity.getThis.runOnUiThread(() -> {
            //DebugLog.log("notify adapter");
                SpotifyPlaylistFragment.getThisPlaylist.tracksAdapter.notifyDataSetChanged();
        });
        }catch(Exception e){
            DebugLog.log("notify adapter error");
            Log.v("samba", Log.getStackTraceString(e));
        }
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
        if (spotifyList) {
            //DebugLog.log("spotifylist");
            if (tracksAdapter != null) {
                //DebugLog.log("tracksAdapter not null");
                MainActivity.getThis.runOnUiThread(() -> {
                    if ((currentTrack >= SpotifyFragment.getThis.data.albumTracks.size()) || (SpotifyFragment.getThis.data.albumTracks.size() != previousLength)) {
                        if (!gettingList) {
                            //DebugLog.log("get updated list");
                            //setCurrentTracklist();
                            try {
                                refreshSpotifyPlaylistInBackground();
                            } catch (Exception e) {
                                Log.v("samba", Log.getStackTraceString(e));
                            }
                        } else
                        /*new Handler().postDelayed(() -> {
                            //setCurrentTracklist();
                            gettingList=false;
                        }, 2000);*/
                            DebugLog.log("do not get updated list, list busy");
                        //throw new AssertionError();
                    } else
                        try {
                            //DebugLog.log("set trak to "+currentTrack);
                            if (!gettingList) {
                                tracksAdapter.setCurrentItem(currentTrack);
                                tracksAdapter.notifyDataSetChanged();
                            }

                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }
                });
            }

        }
    }
}
