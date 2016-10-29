package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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

    public static TracksSpotifyPlaylist getInstance() {
        return ourInstance;
    }

    private TracksSpotifyPlaylist() {
        tracks = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Track> getTracks() {
        return tracks;
    }
    private boolean gettingTracks;

    public void triggerPlaylist(SpotifyPlaylistInterface spotifyPlaylistInterface){
        this.spotifyPlaylistInterface=spotifyPlaylistInterface;
        if (!gettingTracks)
            new Thread() {

                @Override
                public void run() {
                    DebugLog.log("start refresh");
                    gettingTracks=true;
                    ArrayList<String> albumList1=new ArrayList<>();
                    ArrayList<PlaylistItem> albumTracks1=new ArrayList<>();
                    int nr = 0;
                    JSONArray items=null;
                    while (((items == null) && (nr < 3)))
                    {

                        JSONArray playlist = SpotifyFragment.getPlaylist();
                        items = playlist;
                        if (items==null)

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
                        //albumList1.clear();
                        //albumTracks1.clear();
                        String prevAlbum = "";
                        for (int i = 0; i < items.length(); i++) {
                            try {
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
                                    }
                                }
                                if (pi2 == null) {
                                    if (trackid.length() > 0) {
                                        Track t = SpotifyFragment.getTrack(trackid);
                                        tracks.add(t);
                                        SpotifyFragment.getThis.data.previousTracksPlaylist.add(t);
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
                                        new SpotifyFragment.DownLoadImageUrlTask() {
                                            @Override
                                            public void setUrl(String logo) {
                                                pi.url = logo;
                                            }
                                        }.execute(t.album.id);
                                        //perhaps image-id already present?
                                        pi.url = SpotifyFragment.getImageUrl(t.album.images);
                                        //rest of properties
                                        pi.id = t.id;
                                        pi.trackNumber = t.track_number;
                                        int time = new Double(t.duration_ms / 1000).intValue();
                                        pi.time = time;

                                        albumList1.add(pi.text);
                                        albumTracks1.add(pi);
                                        SpotifyFragment.getThis.data.previousAlbumTracks.add(pi);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                        for (Track t : tracks) {

                        }
                    gettingTracks=false;
                    if (spotifyPlaylistInterface!=null)
                        spotifyPlaylistInterface.spotifyPlaylistReturn(albumList1,albumTracks1);
                    DebugLog.log("end refresh");
                }
            }.start();

    }

    private List<Track> tracks;
    //private ArrayList<PlaylistItem> tracks;
}
