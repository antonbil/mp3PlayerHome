package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.adapters.FavoriteListAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.HistoryListview;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Server;
import examples.quickprogrammingtips.com.tablayout.tools.Utils;


public class SelectFragment extends Fragment implements FavoritesInterface{
    public static SelectFragment getThis;
    static final int STATIC_RESULT_SELECT =3; //positive > 0 integer.
    private Logic logic;
    //ArrayList<Favorite> favorites;
    //ListView favoriteListView;
    //FavoriteListAdapter favoriteListAdapter;
    ArrayList<FavoritesListItem>favoritesListItemArray=new ArrayList<>();

    private ArrayList<Server>servers=Server.servers;//Server.servers.get(Server.getServer(getActivity())).url;
    //private boolean regularFavoritesVisible=false;
    //private ListView favoritespotifyListView;
    private FavoriteListAdapter favoritespotifyListAdapter;
    //private ArrayList<Favorite> spotifyfavorites;
    //private boolean regularspotifyFavoritesVisible=true;

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        getThis=this;
            servers.toArray();
            //favorites = new ArrayList<>();
        //spotifyfavorites = new ArrayList<>();

            //test records to db
            /*FavoriteRecord fv=new FavoriteRecord("abc", "abcpath", "2nd edition");
            fv.save();
            fv=new FavoriteRecord("abc2", "abcpath2", "2nd edition");
            fv.save();*/

            // Inflate the layout for this fragment
            logic =((MainActivity)getActivity()).getLogic();
            View view = inflater.inflate(R.layout.fragment_select, container, false);
        try{
            final SharedPreferences app_preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Get the value for the run counter
            int server = Server.getServer(getActivity());
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.select_radioGroup);
            radioGroup.check(servers.get(server).code);
            for (int i = 0; i < radioGroup .getChildCount(); i++) {
                ((RadioButton) radioGroup.getChildAt(i)).setText(servers.get(i).description);
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {


                @Override

                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    // find which radio button is selected
                    for (int i = 0; i < servers.size(); i++) {
                        if (checkedId == servers.get(i).code) {
                            setAddress(servers.get(i).url);
                            Log.v("samba", servers.get(i).url);

                            Server.setServer(i, getActivity());
                        }
                    }

                }

            });

            /*TextView tv=(TextView)view.findViewById(R.id.favoriteTextlistView);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    regularFavoritesVisible = !regularFavoritesVisible;
                    getFavorites();
                }
            });*/
        /*TextView tv2=(TextView)view.findViewById(R.id.favoritespotifyTextlistView);
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regularspotifyFavoritesVisible = !regularspotifyFavoritesVisible;
                getFavorites();
            }
        });*/
            /*favoriteListView = (android.widget.ListView) view.findViewById(R.id.selectlistView);
            favoriteListAdapter = new FavoriteListAdapter(getActivity(), this, false,favorites);
            favoriteListView.setAdapter(favoriteListAdapter);*/
        /*favoritespotifyListView = (android.widget.ListView) view.findViewById(R.id.selectspotifylistView);
        favoritespotifyListAdapter = new FavoriteListAdapter(getActivity(), this, false,spotifyfavorites);
        favoritespotifyListView.setAdapter(favoriteListAdapter);*/

            //FavoritesListItem favoritesSpotifyListItem = new FavoritesListItem(this, view, "spotify", "sp");
            favoritesListItemArray.add(new FavoritesListItem(this, view, "favorites", "1",false));
            favoritesListItemArray.add(new FavoritesListItem(this, view, "spotify", "2",true));
            for (int i=0;i<Favorite.categoryIds.size();i++) {
                final FavoritesListItem favoritesListItem = new FavoritesListItem(this, view, Favorite.getCategory(i), Favorite.categoryIds.get(i),false);
                favoritesListItemArray.add(favoritesListItem);
            }

            for (final FavoritesListItem item: favoritesListItemArray) {
                item.favoriteTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.toggleVisible();
                        getFavorites();
                    }
                });
                //favoritesListItemArray.add(favoritesListItem);
            }
            getFavorites();
    } catch (Exception e) {
        Log.v("samba", Log.getStackTraceString(e));
        //Log.v("samba", Log.getStackTraceString(e));
    }

            return view;
        }

    public void getFavorites() {

        //favorites.clear();
        //spotifyfavorites.clear();
        //https://open.spotify.com/user/koenpoolman/playlist/0ucT4Y07hYtIcJrvunGstF
        /*spotifyfavorites.add(new Favorite("https://open.spotify.com/user/redactie_oor/playlist/3N9rTO6YG7kjWETJGOEvQY", "oor11", "Spotify"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        spotifyfavorites.add(new Favorite("spotify://redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY", "oor11Geheel", "Spotify"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        spotifyfavorites.add(new Favorite("https://open.spotify.com/user/nederlandse_top_40/playlist/5lH9NjOeJvctAO92ZrKQNB", "nltop40", "Spotify"));
        spotifyfavorites.add(new Favorite("https://open.spotify.com/user/redactie_oor/playlist/47Uk3e6OMl4z1cKjMY4271", "oor: redactie", "Spotify"));
        spotifyfavorites.add(new Favorite("https://open.spotify.com/user/koenpoolman/playlist/1WCuVrwkQbZZw6qmgockjv", "oor rockt", "Spotify"));
        spotifyfavorites.add(new Favorite("https://open.spotify.com/user/koenpoolman/playlist/0ucT4Y07hYtIcJrvunGstF", "oor danst", "Spotify"));*/

        List<FavoriteRecord> favoritesDisk = FavoriteRecord.listAll(FavoriteRecord.class);
        for (FavoritesListItem fi:favoritesListItemArray)
        fi.favoritesAdded.clear();
        ArrayList<Favorite> favoritesSpotifyListItem = favoritesListItemArray.get(0).favoritesAdded;
        favoritesSpotifyListItem.add(new Favorite("00tags/favorites", "favorites", "1"));
        favoritesSpotifyListItem.add(new Favorite("00tags/newest", "newest", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2014/", "2014", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2015/", "2015", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2016/", "2016", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/", "years", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/Soul/", "Soul", "1"));
        favoritesSpotifyListItem = favoritesListItemArray.get(1).favoritesAdded;
        //https://open.spotify.com/user/1218062195/playlist/2AxpY5WlA9JAn4Vcpx8GSV
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"redactie_oor/playlist/3N9rTO6YG7kjWETJGOEvQY", "oor11", "2"));
        //https://open.spotify.com/user/spotify/playlist/3Yrvm5lBgnhzTYTXx2l55x
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY", "oor11Geheel", "2"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"nederlandse_top_40/playlist/5lH9NjOeJvctAO92ZrKQNB", "nltop40", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"spotify/playlist/3Yrvm5lBgnhzTYTXx2l55x", "new releases", "2"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"redactie_oor/playlist/47Uk3e6OMl4z1cKjMY4271", "oor: redactie", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"koenpoolman/playlist/1WCuVrwkQbZZw6qmgockjv", "oor rockt", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"koenpoolman/playlist/0ucT4Y07hYtIcJrvunGstF", "oor danst", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"122978137/playlist/3dixZSVLSak9apekDzw8r5", "ambient1", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"1249149618/playlist/5r977N6ZbHTM3Pm5CpzXzJ", "ambient2", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"spotify/playlist/0lBxkSj5VzRfcy8gxFUB5E", "ambient3", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"1218062195/playlist/2AxpY5WlA9JAn4Vcpx8GSV", "classical", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"spotify/playlist/024GOC1aaJzcF0YrTGdeSu", "Composer weekly", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"spotify/playlist/4gWfh2NYhzzJ9NGP9D9fHE", "Classical new releases", "2"));
        //https://open.spotify.com/user/spotify/playlist/4gWfh2NYhzzJ9NGP9D9fHE
        //https://open.spotify.com/user/spotify/playlist/0lBxkSj5VzRfcy8gxFUB5E

        for (FavoriteRecord fav:favoritesDisk){
            //split hier description voor aparte elementen. scheidingsteken: ;;
            String[] parts=fav.description.split(";;");
            String sortkey="";
            String description=parts[0];
            try{
                sortkey=parts[1];

            } catch (Exception e){

            }
            Favorite favnew=new Favorite(fav.url,description,description,sortkey);
            favnew.setRecord(fav);
            if (!MainActivity.filterSpotify||(MainActivity.filterSpotify&&favnew.isSpotifyItem()))
            for (FavoritesListItem favItem:favoritesListItemArray){
                //Log.v("samba", favItem.selectlistViewcode + " vs " + fav.category);
                if (favItem.selectlistViewcode.equals(fav.category))
                    favItem.favoritesAdded.add(favnew);
            }

        }
        for (FavoritesListItem fi:favoritesListItemArray)
            if(fi.favoritesAdded.size()>0)fi.favoriteTextView.setVisibility(View.VISIBLE); else fi.favoriteTextView.setVisibility(View.GONE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if (regularFavoritesVisible)
                    Utils.setDynamicHeight(favoriteListView, 0);
                    //setHeightListView(favoriteListAdapter, favoriteListView,favorites);
                    else
                    setListViewHeight(favoriteListView, 0);*/
                /*if (regularspotifyFavoritesVisible)
                    Utils.setDynamicHeight(favoritespotifyListView, 0);
                    //setHeightListView(favoriteListAdapter, favoriteListView,favorites);
                else
                    setListViewHeight(favoritespotifyListView, 0);*/
                for (FavoritesListItem fi:favoritesListItemArray) {
                    Collections.sort(fi.favoritesAdded, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Favorite mp1 = (Favorite) o1;
                            Favorite mp2 = (Favorite) o2;
                            String s1 = (mp1.getSortkey() + mp1.getDescription()).toLowerCase();
                            String s2 = (mp2.getSortkey() + mp2.getDescription()).toLowerCase();
                            return s1.compareTo(s2);
                        }
                    });
                    if (fi.isVisible())
                    //setHeightListView(fi.favoritesAddedListAdapter, fi.favoritesAddedListView, fi.favoritesAdded);
                    Utils.setDynamicHeight(fi.favoritesAddedListView, 0);
                    else setListViewHeight(fi.favoritesAddedListView, 0);
                }
                /*Collections.sort(favoritesListItemArray, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        FavoritesListItem mp1 = (FavoritesListItem) o1;
                        FavoritesListItem mp2 = (FavoritesListItem) o2;
                        //String a = mp1.selectlistViewcode;
                        return (mp1.selectlistViewcode.compareTo((mp2.selectlistViewcode)));
                    }
                });*/


            }
        });


    }

    /*public void setHeightListView(FavoriteListAdapter nyAdapter, ListView myListView, ArrayList<Favorite> favorites) {
        nyAdapter.notifyDataSetChanged();
        int height = 130 * favorites.size();
        setListViewHeight(myListView, height);
    }*/

    public void setListViewHeight(ListView myListView, int height) {
        ViewGroup.LayoutParams params = myListView.getLayoutParams();

        params.height = height;
        myListView.setLayoutParams(params);
        myListView.requestLayout();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

    }
    public void setAddress(String address) {
        logic.openServer(address);
        logic.getMpc().setMPCListener((MainActivity) getActivity());
        MainActivity.getThis.playlistGetContent();
    }

    @Override
    public void favoritesCall(Favorite favorite, String id) {
        Log.v("samba", favorite.getUri());
        //spotify://
        FragmentActivity activity = this.getActivity();
        if (favorite.getUri().startsWith(Favorite.SPOTIFYPRIVATEPLAYLIST)){
            try {
                final ProgressDialog loadingdialog;
                loadingdialog = ProgressDialog.show(activity,
                        "","Loading, please wait",true);
                SpotifyActivity.clearSpotifyPlaylist();
                new SpotifyActivity.getEntirePlaylistFromSpotify(favorite.getUri().replace(Favorite.SPOTIFYPRIVATEPLAYLIST,""),MainActivity.getThis){
                    @Override
                    public void atLast() {
                        loadingdialog.dismiss();
                        MainActivity.getThis.startPlaylistSpotify();
                    }
                }.run();
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
                //Log.v("samba", Log.getStackTraceString(e));
            }

        }
        else
        if (favorite.getUri().startsWith(Favorite.SPOTIFYPLAYLISTPREFIX)){
            try {
                final ProgressDialog loadingdialog;
                loadingdialog = ProgressDialog.show(activity,
                        "","Loading, please wait",true);
                SpotifyActivity.clearSpotifyPlaylist();
                new SpotifyActivity.addExternalPlaylistToSpotify(favorite.getUri(),MainActivity.getThis){
                    @Override
                    public void atLast() {
                        loadingdialog.dismiss();
                        MainActivity.getThis.startPlaylistSpotify();
                    }
                }.run();
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
                //Log.v("samba", Log.getStackTraceString(e));
            }

        }
        else {
            if (id.equals("edit")) {
                if (favorite.getRecord() != null) {
                    Intent intent = new Intent(getActivity(), EditFavoriteActivity.class);
                    intent.putExtra("id", (int) (favorite.getRecord().getId() + 0));
                    intent.putExtra("url", favorite.getRecord().url);
                    intent.putExtra("description", favorite.getDescription());
                    intent.putExtra("category", favorite.getCategory());
                    intent.putExtra("sortkey", favorite.getSortkey());
                    startActivityForResult(intent, STATIC_RESULT_SELECT);
                }


            } else if (id.equals("delete")) {
                if (favorite.getRecord() != null) {

                    FavoriteRecord book = FavoriteRecord.findById(FavoriteRecord.class, favorite.getRecord().getId());
                    book.delete();
                    getFavorites();
                }

            }
            else
            if (favorite.getUri().startsWith(Favorite.SPOTIFYALBUM)){
                try {
                    final ProgressDialog loadingdialog;
                    loadingdialog = ProgressDialog.show(activity,
                            "","Loading, please wait",true);
                    SpotifyActivity.clearSpotifyPlaylist();
                    String[] a = favorite.getDescription().split("-");
                    //SpotifyActivity.artistName=a[0];
                    new SpotifyActivity.addAlbumWithIdToSpotify(favorite.getUri().replace(Favorite.SPOTIFYALBUM,""),a[0],a[1],MainActivity.getThis){
                        @Override
                        public void atLast() {
                            loadingdialog.dismiss();

                            MainActivity.getThis.startPlaylistSpotify();

                        }
                    }.run();
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                    //Log.v("samba", Log.getStackTraceString(e));
                }

            }
            else {
                String uri = favorite.getUri();
                if (uri.startsWith(Favorite.SMBPREFIX)) {
                    if (!id.equals("add to playlist")) {//todo add item add to playlist
                        Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
                        logic.getHistory().add(new HistoryListview(uri, 0));
                        ((MainActivity) getActivity()).selectTab(1);
                    }
                } else {
                    if (id.equals("add to playlist")) {
                        String command = ("add \"" + uri + "\"");
                        //Log.v("samba",command);
                        logic.getMpc().enqueCommands(new ArrayList<>(Collections.singletonList(command)));
                    } else {
                        logic.getHistoryMpd().add(new HistoryListview(uri, 0));
                        Log.v("samba",uri);
                        ((MainActivity) getActivity()).selectTab(2);
                    }
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v("samba","in fragment");
        if (requestCode == STATIC_RESULT_SELECT) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // this is successful mission, do with it.
                //favorites.clear();
                getFavorites();


                }
        }

else//added on 21-6-2016
        super.onActivityResult(requestCode, resultCode, data);

    }

    private class FavoritesListItem {
        /*
        generates an item that consists of textview above, listview and listadapter.
        a click on the textview hides the listview, or makes it visible.
        The listview contains favorites
         */
        private boolean visible=true;
        public  String selectlistViewcode;
        public  ListView favoritesAddedListView;
        public  FavoriteListAdapter favoritesAddedListAdapter;
        public ArrayList<Favorite> favoritesAdded;
        public TextView favoriteTextView;
        public FavoritesListItem(SelectFragment selectFragment, View parentView, String listDescription, String selectlistViewcode,boolean visible) {
            this.selectlistViewcode=selectlistViewcode;

            LinearLayout LL = new LinearLayout(selectFragment.getActivity());
            LL.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

            LL.setPadding(10, 10, 10, 10);
            LL.setLayoutParams(LLParams);
            favoriteTextView = new TextView(selectFragment.getActivity());
            favoriteTextView.setText(listDescription);

            LLParams.setMargins(0, 10, 10, 0);
            favoriteTextView.setLayoutParams(LLParams);

            final float scale = getResources().getDisplayMetrics().density;
            int dpHeightInPx = (int) (40 * scale);
            favoriteTextView.setMinHeight(dpHeightInPx);

            favoriteTextView.setGravity(Gravity.CENTER_VERTICAL);
            favoriteTextView.setBackgroundColor(Color.parseColor("#4A9C67"));
            LL.addView(favoriteTextView);
            this.favoritesAdded = new ArrayList<>();
            this.favoritesAddedListView = new ListView(selectFragment.getActivity());
            this.favoritesAddedListAdapter = new FavoriteListAdapter(selectFragment.getActivity(), selectFragment, true, this.favoritesAdded);
            this.favoritesAddedListView.setAdapter(favoritesAddedListAdapter);

            LL.addView(this.favoritesAddedListView);
            LinearLayout rl=((LinearLayout) parentView.findViewById(R.id.favoritesLinearLayout));
            rl.addView(LL);
            this.visible=visible;

        }

        public FavoritesListItem(SelectFragment selectFragment, View parentView, String listDescription, String selectlistViewcode) {
            this( selectFragment,  parentView,  listDescription,  selectlistViewcode,true);
        }
        public void toggleVisible(){
            visible=!visible;
        }
        public boolean isVisible(){
            return visible;
        }
    }
}
