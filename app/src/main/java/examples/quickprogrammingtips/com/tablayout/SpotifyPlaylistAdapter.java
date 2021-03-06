package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by anton on 17-10-16.
 * Adapter to access playlist of Spotify
 */
class SpotifyPlaylistAdapter extends PlanetAdapter {
    private ListView albumsListview;
    private ArrayList<String> albumList;
    private ArrayList<PlaylistItem> albumTracks;

    SpotifyPlaylistAdapter(ArrayList<String> albumList, MainActivity getThis, ArrayList<PlaylistItem> albumTracks, ListView albumsListview) {

        super(albumList,getThis,albumTracks);
        this.albumList=albumList;
        this.albumTracks=albumTracks;
        this.albumsListview=albumsListview;
    }

        @Override
        public void removeUp(int counter) {
        duplicateLists();

        SpotifyFragment.removeUplist(this, counter, MainActivity.getInstance());
            for (int i=0;i<counter;i++)
            albumList.remove(0);
            MainActivity.getInstance().runOnUiThread(() -> {
                this.notifyDataSetChanged();
            });
    }

    @Override
    public void removeAll() {
        SpotifyFragment.clearSpotifyPlaylist();
        albumList.clear();
        SpotifyFragment.getData().tracksPlaylist.clear();
        MainActivity.getInstance().runOnUiThread(() -> {
            this.notifyDataSetChanged();
        });
    }

    @Override
        public void onClickFunc(int counter) {
        try {
            new SpotifyInterface().previousTrack.id = "";
        } catch (Exception e) {
            //
        }
        duplicateLists();
        SpotifyFragment.stopMpd();
        SpotifyFragment.playlistGotoPosition(counter);
    }

        @Override
        public void removeDown(int counter) {
        duplicateLists();
        SpotifyFragment.removeDownlist(this, counter, MainActivity.getInstance());
            for (int i=counter;i<albumList.size();i++)
                albumList.remove(counter);
            MainActivity.getInstance().runOnUiThread(() -> {
                this.notifyDataSetChanged();
            });

    }

        @Override
        public void removeAlbum(int counter) {
        duplicateLists();
            //todo refresh playlist
        SpotifyFragment.removeAlbum(this, counter, MainActivity.getInstance());

    }

    private void duplicateLists() {
        SpotifyFragment.getData().albumList = albumList;
        SpotifyFragment.getData().albumTracks = albumTracks;
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
        //todo refresh playlist

    }

    @Override
    public void displayArtist(int counter) {
        MainActivity.getInstance().callSpotify(SpotifyFragment.getData().tracksPlaylist.get(counter).artists.get(0).name);


    }

    @Override
    public void recommendation(int counter) {
        MainActivity.getRecommendation(SpotifyFragment.getData().tracksPlaylist.get(counter).artists.get(0).name);


    }

    @Override
    public void displayArtistWikipedia(int counter) {
        String s = SpotifyFragment.getData().tracksPlaylist.get(counter).artists.get(0).name;
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
        try {
            SpotifyFragment.getAlbumtracksFromSpotify(SpotifyFragment.getData().tracksPlaylist.get(counter).album.id, SpotifyFragment.getData().tracksPlaylist.get(counter).artists.get(0).name
                    , MainActivity.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void transferPlaylist() {

        ArrayList<Track> tracks=new ArrayList<>();
        int currentTrack=SpotifyFragment.currentTrack;
        int trackPosition=SpotifyFragment.getTime();
        tracks.addAll(SpotifyFragment.getData().tracksPlaylist);
        new SetAndPlayOnServer(parent.getContext()/*SpotifyPlaylistFragment.activityThis*/){
            @Override
            public void atFirst(){
                SpotifyFragment.stopSpotifyPlaying(SpotifyFragment.ipAddress);
            }
            @Override
            public void atEnd(){
                try{
                    SpotifyFragment.clearSpotifyPlaylist();
                    for (Track t:tracks) {
                        String prefix = "spotify:track:";
                        String uri = t.id;
                        if (uri.startsWith("spotify")) prefix = "";
                        SpotifyFragment.AddSpotifyItemToPlaylist(prefix, uri);
                    }
                    MainActivity.getInstance().getLogic().getMpc().stop();
                    //SpotifyFragment.busyupdateSongInfo=false;
                    SpotifyFragment.stopMpd();
                    SpotifyFragment.playAtPosition(currentTrack);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SpotifyFragment.seekPositionSpotify(SpotifyFragment.ipAddress, trackPosition*1000);
                        }
                    }, 400);

                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }
        };
    }

    @Override
    public void addAlbumNoplay(int counter) {
    }

    @Override
    protected void infoAlbum(int position, String imageUrl) {
        String albumid=SpotifyFragment.getData().tracksPlaylist.get(position).album.id;
        String albumname=SpotifyFragment.getData().tracksPlaylist.get(position).name;
        try {
            String artist = SpotifyFragment.getData().tracksPlaylist.get(position).artists.get(0).name;
            DebugLog.log("artist:" + artist);
        }
        catch (Exception e){}
        String url = SpotifyFragment.getData().tracksPlaylist.get(position).album.images.get(0).url;

        SpotifyFragment.infoAlbum(albumid,albumname, url, MainActivity.getInstance());

    }

}
