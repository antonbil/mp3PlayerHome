package examples.quickprogrammingtips.com.tablayout;

import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by anton on 17-10-16.
 */
public class SpotifyPlaylistAdapter extends PlanetAdapter {
    private ListView albumsListview;
    private ArrayList<String> albumList;
    private ArrayList<PlaylistItem> albumTracks;

    public SpotifyPlaylistAdapter(ArrayList<String> albumList, MainActivity getThis, ArrayList<PlaylistItem> albumTracks,ListView albumsListview) {

        super(albumList,getThis,albumTracks);
        this.albumList=albumList;
        this.albumTracks=albumTracks;
        this.albumsListview=albumsListview;
    }

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
        }
        duplicateLists();
        SpotifyFragment.stopMpd();
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

}