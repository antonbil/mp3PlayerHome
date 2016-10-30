package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by anton on 29-10-16.
 */
public class TracksSpotifyPlaylist {
    private static TracksSpotifyPlaylist ourInstance = new TracksSpotifyPlaylist();
    private SpotifyPlaylistInterface spotifyPlaylistInterface;
    private boolean changed=false;
    private ArrayList<String> albumList1 = new ArrayList<>();
    private ArrayList<PlaylistItem> albumTracks1 = new ArrayList<>();

    public static TracksSpotifyPlaylist getInstance() {
        return ourInstance;
    }

    private TracksSpotifyPlaylist() {
        tracks = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Track> getTracks() {
        return tracks;
    }
    private static boolean gettingTracks;
    private ArrayList<SpotifyPlaylistInterface> listeners=new ArrayList<>();
    private int inbetween=5000;
    public void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface, boolean forceDisplay){
        if (forceDisplay&&albumList1.size()>0)
            spotifyPlaylistInterface.spotifyPlaylistReturn(albumList1, albumTracks1,true);
        triggerPlaylist(spotifyPlaylistInterface,5000);

    }
    public void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface){
        triggerPlaylist(spotifyPlaylistInterface,5000);

    }
        public void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface, int inbetween)
    {
        this.spotifyPlaylistInterface=spotifyPlaylistInterface;
        boolean there=false;
        for (SpotifyPlaylistInterface spi:listeners)
            if (spotifyPlaylistInterface==spi)there=true;
        if (!there){
            this.changed=true;
            listeners.add(spotifyPlaylistInterface);
        }
        if (!gettingTracks) {
            gettingTracks = true;
            this.inbetween = inbetween;

            new Thread() {

                @Override
                public void run() {
                    //DebugLog.log("start refresh");
                    int nr = 0;
                    JSONArray items = null;
                    while (((items == null) && (nr < 3))) {

                        JSONArray playlist = SpotifyFragment.getPlaylist();
                        items = playlist;
                        if (items == null)

                            try {
                                Thread.sleep(1000);
                                //only do the looper.loop first time Thread is executed.

                            } catch (Exception e) {
                                DebugLog.log("error2");
                                Log.v("samba", Log.getStackTraceString(e));
                            }
                    }

                    if (items != null)

                    {
                        //save items to new playlist
                        tracks.clear();
                        albumList1.clear();
                        albumTracks1.clear();
                        String prevAlbum = "";
                        int i = 0;
                        while ((i < items.length()) && gettingTracks) {
                            try {
                                //Thread.sleep(20);
                                String trackid = "";
                                PlaylistItem pi2 = null;
                                JSONObject o = null;
                                o = items.getJSONObject(i);
                                trackid = o.getJSONObject("track").getString("uri").replace("spotify:track:", "");
                                if (trackid.length() == 0) continue;
                                for (int j = 0; j < SpotifyFragment.getThis.data.previousAlbumTracks.size(); j++) {
                                    PlaylistItem pi = SpotifyFragment.getThis.data.previousAlbumTracks.get(j);
                                    if (pi.id.equals(trackid)) {
                                        pi2 = pi;
                                        tracks.add(SpotifyFragment.getThis.data.previousTracksPlaylist.get(j));
                                        albumList1.add(pi.text);
                                        albumTracks1.add(pi);
                                    }
                                }
                                if (pi2 == null) {
                                    prevAlbum = createNewTrack(albumList1, albumTracks1, prevAlbum, trackid);
                                }
                                //DebugLog.log("nr:"+i);
                                if (i == inbetween) {
                                    updateListview(albumList1, albumTracks1);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    }
                    gettingTracks = false;
                    updateListview(albumList1, albumTracks1);
                    //DebugLog.log("end refresh");
                }
            }.start();
        }

    }

    public void updateListview(ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1) {
        if (changed) {

            //update global tracks
            List<Track> mylist = SpotifyFragment.data.tracksPlaylist;
            mylist.clear();
            for (Track t:tracks)
                mylist.add(t);
            //update listeners
            if (spotifyPlaylistInterface != null)
                spotifyPlaylistInterface.spotifyPlaylistReturn(albumList1, albumTracks1,false);
                else
            for (SpotifyPlaylistInterface listener:listeners){
                try{
                    if (listener!=null)listener.spotifyPlaylistReturn(albumList1, albumTracks1,false);
                }catch (Exception e){}
            }

            changed=false;
        }
    }

    public String createNewTrack(ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1, String prevAlbum, String trackid) {
        if (trackid.length() > 0) {
            Track t = SpotifyFragment.getTrack(trackid);
            tracks.add(t);
            SpotifyFragment.getThis.data.previousTracksPlaylist.add(t);
            final PlaylistItem pi = new PlaylistItem();
            //check for change in album-name
            String extra = "";
            pi.pictureVisible = false;
            try {
                String name = t.album.name;
                if (!prevAlbum.startsWith(name)) {
                    extra = String.format("(%s-%s)", t.artists.get(0).name, name);
                    prevAlbum = name;
                    pi.pictureVisible = true;
                }
                //get image-id
                new SpotifyFragment.DownLoadImageUrlTask() {
                    @Override
                    public void setUrl(String logo) {
                        pi.url = logo;
                    }
                }.execute(t.album.id);
                //perhaps image-id already present?
                pi.url = SpotifyFragment.getImageUrl(t.album.images);

            } catch (Exception e) {
                DebugLog.log("error createNewTrack");
                //gettingTracks=false;
                return prevAlbum;
                //Log.v("samba", Log.getStackTraceString(e));
                //return prevAlbum;
            }
            //album-name can become part of title
            pi.text = t.name + extra;
            //rest of properties
            pi.id = t.id;
            pi.trackNumber = t.track_number;
            int time = new Double(t.duration_ms / 1000).intValue();
            pi.time = time;

            albumList1.add(pi.text);
            albumTracks1.add(pi);
            SpotifyFragment.getThis.data.previousAlbumTracks.add(pi);
            changed=true;
        }
        return prevAlbum;
    }

    private List<Track> tracks;
    //private ArrayList<PlaylistItem> tracks;
}
