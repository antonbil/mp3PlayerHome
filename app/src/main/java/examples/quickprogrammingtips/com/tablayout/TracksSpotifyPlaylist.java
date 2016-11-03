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
 * Singleton to get playlist-data from mopidy
 */
class TracksSpotifyPlaylist {
    private static TracksSpotifyPlaylist ourInstance = new TracksSpotifyPlaylist();
    private SpotifyPlaylistInterface spotifyPlaylistInterface;
    //private boolean changed=false;
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
    void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface, boolean forceDisplay){
        if (forceDisplay&&albumList1.size()>0)
            spotifyPlaylistInterface.spotifyPlaylistReturn(albumList1, albumTracks1,true);
        triggerPlaylist(spotifyPlaylistInterface,5000);

    }

    void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface){
        triggerPlaylist(spotifyPlaylistInterface,5000);

    }
        void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface, int inbetween)
    {
        this.spotifyPlaylistInterface=spotifyPlaylistInterface;
        boolean there=false;
        for (SpotifyPlaylistInterface spi:listeners)
            if (spotifyPlaylistInterface==spi)there=true;
        if (!there){
            //this.changed=true;
            listeners.add(spotifyPlaylistInterface);
        }
        if (!gettingTracks) {
            gettingTracks = true;

            new Thread() {

                @Override
                public void run() {
                    int nr = 0;
                    JSONArray items = null;
                    while (((items == null) && (nr < 3))) {

                        items = SpotifyFragment.getPlaylist();
                        nr++;
                        if (items == null)

                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                DebugLog.log("error2");
                                Log.v("samba", Log.getStackTraceString(e));
                            }
                    }

                    tracks.clear();
                    albumList1.clear();
                    albumTracks1.clear();
                    if (items != null)

                    {
                        //save items to new playlist
                         String prevAlbum = "";
                        int i = 0;
                        while ((i < items.length()) && gettingTracks) {
                            try {
                                String trackid;
                                PlaylistItem pi2 = null;
                                JSONObject o = items.getJSONObject(i);
                                trackid = o.getJSONObject("track").getString("uri").replace("spotify:track:", "");
                                if (trackid.length() == 0) continue;
                                for (int j = 0; j < SpotifyFragment.getData().previousAlbumTracks.size(); j++) {
                                    PlaylistItem pi = SpotifyFragment.getData().previousAlbumTracks.get(j);
                                    if (pi.id.equals(trackid)) {
                                        pi2 = pi;
                                        tracks.add(SpotifyFragment.getData().previousTracksPlaylist.get(j));
                                        albumList1.add(pi.text);
                                        albumTracks1.add(pi);
                                    }
                                }
                                if (pi2 == null) {
                                    prevAlbum = createNewTrack(albumList1, albumTracks1, prevAlbum, trackid);
                                }
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
                }
            }.start();
        } else{
            updateListview(albumList1, albumTracks1);

        }

    }

    private void updateListview(ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1) {

            //update global tracks
            List<Track> mylist = SpotifyFragment.getData().tracksPlaylist;
            mylist.clear();
            mylist.addAll(tracks);
            //update listeners
            if (spotifyPlaylistInterface != null)
                spotifyPlaylistInterface.spotifyPlaylistReturn(albumList1, albumTracks1,false);
                else
            for (SpotifyPlaylistInterface listener:listeners){
                try{
                    if (listener!=null)listener.spotifyPlaylistReturn(albumList1, albumTracks1,false);
                }catch (Exception e){
                    //changed=false;
                    }
            }
    }

    private String createNewTrack(ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1, String prevAlbum, String trackid) {
        if (trackid.length() > 0) {
            Track t = SpotifyFragment.getTrack(trackid);
            tracks.add(t);
            SpotifyFragment.getData().previousTracksPlaylist.add(t);
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
                return prevAlbum;
            }
            //album-name can become part of title
            pi.text = t.name + extra;
            //rest of properties
            pi.id = t.id;
            pi.trackNumber = t.track_number;
            pi.time = Double.valueOf(t.duration_ms / 1000).intValue();

            albumList1.add(pi.text);
            albumTracks1.add(pi);
            SpotifyFragment.getData().previousAlbumTracks.add(pi);
            //changed=true;
        }
        return prevAlbum;
    }

    private List<Track> tracks;
    //private ArrayList<PlaylistItem> tracks;
}
