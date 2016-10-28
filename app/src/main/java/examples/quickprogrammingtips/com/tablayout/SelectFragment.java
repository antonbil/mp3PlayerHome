package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Timer;
import java.util.TimerTask;

import examples.quickprogrammingtips.com.tablayout.adapters.FavoriteListAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.HistoryListview;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Server;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import examples.quickprogrammingtips.com.tablayout.tools.Utils;


public class SelectFragment extends Fragment implements FavoritesInterface{
    public static SelectFragment getThis;
    static final int STATIC_RESULT_SELECT =3; //positive > 0 integer.
    private Logic logic;
    private ArrayList<FavoritesListItem>favoritesListItemArray;

    private ArrayList<Server>servers=Server.servers;
    private RadioGroup radioGroup;
    private RadioGroup.OnCheckedChangeListener radioGroupListener;
    private View selectView;
    private int currentServer;
    private static ProgressDialog loadingdialog;

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        //DebugLog.log( "select create view");
        getThis=this;
        servers.toArray();


        // Inflate the layout for this fragment
        logic =((MainActivity)getActivity()).getLogic();
        selectView = inflater.inflate(R.layout.fragment_select, container, false);
        try{
            // Get the value for the run counter
            radioGroup = (RadioGroup) selectView.findViewById(R.id.select_radioGroup);
            setCheck();
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                ((RadioButton) radioGroup.getChildAt(i)).setText(servers.get(i).description);
            }
            radioGroupListener = (group, checkedId) -> {

                // find which radio button is selected
                for (int i = 0; i < servers.size(); i++) {
                    if (checkedId == servers.get(i).code) {
                        if (currentServer!=i) {
                            setAddress(servers.get(i).url);
                            Server.setServer(i, getActivity());
                            SpotifyPlaylistFragment.refresh = true;
                        }
                    }
                }

            };
            radioGroup.setOnCheckedChangeListener(radioGroupListener);

            if (favoritesListItemArray==null) {
                initAllFavorites();
                //set spotify shortcuts by default open
                favoritesListItemArray.get(1).visible=true;
            }
            else{
                ArrayList<Boolean>bs=new ArrayList<>();
                for (FavoritesListItem f:favoritesListItemArray)
                bs.add(f.visible);
                initAllFavorites();
                int i=0;
                for (FavoritesListItem f:favoritesListItemArray) {
                    f.visible = bs.get(i);
                    i++;
                }
            }
            checkVisiblityOfLists();
        } catch (Exception e) {
            DebugLog.log( Log.getStackTraceString(e));
        }

        return selectView;
    }

    public void initAllFavorites() {
        View view=this.selectView;
        favoritesListItemArray=new ArrayList<>();

        favoritesListItemArray.add(new FavoritesListItem(this, view, "favorites", "1",false));
        //spotify links by default on second row!
        favoritesListItemArray.add(new FavoritesListItem(this, view, "spotify", "2",false));
        setStaticLinks();

        for (int i = 0; i< Favorite.categoryIdssize(); i++) {
            final FavoritesListItem favoritesListItem = new FavoritesListItem(this, view, Favorite.getCategoryDescription(i), Favorite.categoryIdsget(i),false);
            favoritesListItemArray.add(favoritesListItem);
        }

        for (final FavoritesListItem item: favoritesListItemArray) {
            item.favoriteTextView.setOnClickListener(v -> {
                item.toggleVisible();
                getFavorites();
            });
        }
        getFavorites();
    }

    public void setCheck() {
        currentServer = Server.getServer(getActivity());
        int id = radioGroup.getChildAt(currentServer).getId();
        radioGroup.setOnCheckedChangeListener(null);
        radioGroup.clearCheck();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.getThis.runOnUiThread(() -> {
                    radioGroup.check(id);
                    radioGroup.setOnCheckedChangeListener(radioGroupListener);
                });
            }
        }, 400);

    }

    @Override
    public void onResume(){
        //DebugLog.log( "select onresume");
        super.onResume();
        //initAllFavorites();

    }

    public void getFavorites() {
        //DebugLog.log( "select get favorites");

        //clear previous items
        //leave first 2 items intact
        for (int i=2;i<favoritesListItemArray.size();i++) {
            FavoritesListItem fi=favoritesListItemArray.get(i);
            fi.favoritesAdded.clear();
        }

        List<FavoriteRecord> favoritesDisk = new ArrayList<>();
        try {
            favoritesDisk = FavoriteRecord.listAll(FavoriteRecord.class);
        } catch (Exception e){}


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
                if (favItem.selectlistViewcode.equals(fav.category))
                    favItem.favoritesAdded.add(favnew);
            }

        }
        getActivity().runOnUiThread(() -> {
            //DebugLog.log( "select set favorites");
            for (FavoritesListItem fi:favoritesListItemArray)
                if(fi.favoritesAdded.size()>0)fi.favoriteTextView.setVisibility(View.VISIBLE); else {fi.favoriteTextView.setVisibility(View.GONE);
                    fi.setDistance(0);
                }

            for (int i=2;i<favoritesListItemArray.size();i++) {
                FavoritesListItem fi = favoritesListItemArray.get(i);
                Collections.sort(fi.favoritesAdded, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Favorite mp1 = (Favorite) o1;
                        Favorite mp2 = (Favorite) o2;
                        String s1 = (mp1.getSortkey() + mp1.getDescription()).toLowerCase();
                        String s2 = (mp2.getSortkey() + mp2.getDescription()).toLowerCase();
                        return s1.compareTo(s2);
                    }
                });
            }
            checkVisiblityOfLists();
        });
    }

    public void checkVisiblityOfLists() {
        for (FavoritesListItem fi:favoritesListItemArray){
            if (fi.isVisible())
            Utils.setDynamicHeight(fi.favoritesAddedListView, 0);
            else setListViewHeight(fi.favoritesAddedListView, 1);
            fi.favoritesAddedListAdapter.notifyDataSetChanged();
        }
    }

    public void setStaticLinks() {
        //DebugLog.log( "set static favorites");
        ArrayList<Favorite> favoritesSpotifyListItem = favoritesListItemArray.get(0).favoritesAdded;
        favoritesSpotifyListItem.clear();
        favoritesSpotifyListItem.add(new Favorite("00tags/favorites", "favorites", "1"));
        favoritesSpotifyListItem.add(new Favorite("00tags/newest", "newest", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2014/", "2014", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2015/", "2015", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/2016/", "2016", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/years/", "years", "1"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SMBPREFIX+"192.168.2.8/FamilyLibrary/Soul/", "Soul", "1"));

        //spotify playlists
        favoritesSpotifyListItem = favoritesListItemArray.get(1).favoritesAdded;
        favoritesSpotifyListItem.clear();
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"redactie_oor/playlist/3N9rTO6YG7kjWETJGOEvQY", "oor11", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"redactie_oor:playlist:3N9rTO6YG7kjWETJGOEvQY", "oor11Geheel", "2"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"nederlandse_top_40:playlist:5lH9NjOeJvctAO92ZrKQNB", "nltop40", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"spotify:playlist:3Yrvm5lBgnhzTYTXx2l55x", "new releases", "2"));//"redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY"
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"redactie_oor:playlist:47Uk3e6OMl4z1cKjMY4271", "oor: redactie", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"koenpoolman:playlist:1WCuVrwkQbZZw6qmgockjv", "oor rockt", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"koenpoolman:playlist:0ucT4Y07hYtIcJrvunGstF", "oor danst", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"122978137:playlist:3dixZSVLSak9apekDzw8r5", "ambient1", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"1249149618:playlist:5r977N6ZbHTM3Pm5CpzXzJ", "ambient2", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"spotify:playlist:0lBxkSj5VzRfcy8gxFUB5E", "ambient3", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"1218062195:playlist:2AxpY5WlA9JAn4Vcpx8GSV", "classical", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"spotify:playlist:024GOC1aaJzcF0YrTGdeSu", "Composer weekly", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPRIVATEPLAYLIST+"spotify:playlist:4gWfh2NYhzzJ9NGP9D9fHE", "Classical new releases geheel", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"spotify/playlist/4gWfh2NYhzzJ9NGP9D9fHE", "Classical new releases", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"pureclassical/playlist/3BFUsfko9tiABDX4D211sE", "Pure Classical", "2"));
        favoritesSpotifyListItem.add(new Favorite(Favorite.SPOTIFYPLAYLISTPREFIX+"naxosofficial/playlist/15zSadmJPYq07xXoPg4am1", "Naxos Official", "2"));
        //https://play.spotify.com/user/spotify/playlist/4gWfh2NYhzzJ9NGP9D9fHE

        //spotify://
        //https://open.spotify.com/user/nederlandse_top_40/playlist/5lH9NjOeJvctAO92ZrKQNB
        //https://open.spotify.com/user/spotify/playlist/4gWfh2NYhzzJ9NGP9D9fHE
        //spotify:user:pureclassical:playlist:3BFUsfko9tiABDX4D211sE spotify:user:naxosofficial:playlist:15zSadmJPYq07xXoPg4am1 http://static.echonest.com/playlistminer/index.html​
    }

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
    public static void setAddress(String address) {
        new Thread(() -> {
            try {
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    DebugLog.log( "No connection with "+address);
                    if (!Logic.hasbeen)
                        DebugLog.log( "No connection2 with "+address);
                        //handler.postDelayed(() -> {
                        Toast.makeText(MainActivity.getThis, "No connection with " + Server.servers.get(Server.getServer(MainActivity.getThis)).url, Toast.LENGTH_SHORT).show();
                    //}, 2000);
                }, 400);
            } catch (Exception e){Log.getStackTraceString(e);}
            setServerAddress(address);
        }).start();
    }

    public static void setServerAddress(String address) {
        MainActivity.getThis.getLogic().openServer(address);
        MainActivity.getThis.getLogic().getMpc().setMPCListener(MainActivity.getThis);
        MainActivity.getThis.playlistGetContent();
    }

    @Override
    public void favoritesCall(Favorite favorite, String id) {
        //DebugLog.log( favorite.getUri());
        //spotify://
        FragmentActivity activity = this.getActivity();
        if (favorite.getUri().startsWith(Favorite.SPOTIFYPRIVATEPLAYLIST)){
            try {
                String uri = favorite.getUri().replace(Favorite.SPOTIFYPRIVATEPLAYLIST, "");
                executeExternalSpotifyPlaylist(activity, uri);
                //SpotifyPlaylistFragment.refresh=true;
            } catch (Exception e) {
                DebugLog.log( Log.getStackTraceString(e));
                //DebugLog.log( Log.getStackTraceString(e));
            }

        }
        else
        if (favorite.getUri().startsWith(Favorite.SPOTIFYPLAYLISTPREFIX)){
            try {
                String uri = favorite.getUri();
                executeExternalSpotifyPlaylist30Songs(activity, uri);
                //SpotifyPlaylistFragment.refresh=true;
            } catch (Exception e) {
                DebugLog.log( Log.getStackTraceString(e));
                //DebugLog.log( Log.getStackTraceString(e));
            }

        }
        else {
            if (id.equals("edit")) {
                EditFavoriteActivity.editFavorite(getActivity(),favorite);
                //getFavorites();
            } else if (id.equals("save")) {
                EditFavoriteActivity.saveFavorite(favorite);
                //getFavorites();
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
                    String[] a = favorite.getDescription().split("-");
                    SpotifyFragment.artistName = a[0];
                    if (id.equals("add to playlist")){
                        String prefix="spotify:album:";
                        SpotifyFragment.AddSpotifyItemToPlaylist(prefix, favorite.getUri().replace(Favorite.SPOTIFYALBUM/*"spotifyalbum://"*/,""));

                    } else {
                        SpotifyFragment.getAlbumtracksFromSpotify(favorite.getUri().replace(Favorite.SPOTIFYALBUM/*"spotifyalbum://"*/, ""), a[1], MainActivity.getThis, true);
                        //SpotifyPlaylistFragment.notifyList();
                    }
                } catch (Exception e) {
                    DebugLog.log( Log.getStackTraceString(e));
                }

            }
            else {
                String uri = favorite.getUri();
                if (uri.startsWith(Favorite.SMBPREFIX)) {
                    if (id.equals("add to playlist")) {
                        NetworkShare networkShare=new NetworkShare();
                        if (!uri.endsWith("/"))uri=uri+"/";
                        //DebugLog.log(uri+":add");
                        networkShare.getContent(logic, uri, "add");
                    } else {
                        logic.getHistory().add(new HistoryListview(uri, 0));
                        ((MainActivity) getActivity()).selectTab(1);
                    }
                } else {
                    if (id.equals("add to playlist")) {
                        String command = ("add \"" + uri + "\"");
                        //DebugLog.log(command);
                        logic.getMpc().enqueCommands(new ArrayList<>(Collections.singletonList(command)));
                    } else {
                        logic.getHistoryMpd().add(new HistoryListview(uri, 0));
                        //DebugLog.log(uri);
                        ((MainActivity) getActivity()).selectTab(3);
                    }
                }
            }
        }

    }

    public static void executeExternalSpotifyPlaylist30Songs(Activity activity, final String uri) {
        if (activity!=null) {

            loadingdialog = ProgressDialog.show(activity,
                    "", "Loading, please wait", true);
        }
        SpotifyFragment.clearSpotifyPlaylist();
        new SpotifyFragment.addExternalPlaylistToSpotify(uri, MainActivity.getThis){
            @Override
            public void atLast() {
                if (activity!=null) {
                    loadingdialog.dismiss();
                } else
                    Toast.makeText(MainActivity.getThis, "All tracks added", Toast.LENGTH_SHORT).show();
            }
        }.run();
    }

    public static void executeExternalSpotifyPlaylist(Activity activity, final String uri) {
        if (activity!=null) {
            loadingdialog = ProgressDialog.show(activity,
                    "", "Loading, please wait", true);
        }
        SpotifyFragment.clearSpotifyPlaylist();
        DebugLog.log(uri);
        new SpotifyFragment.getEntirePlaylistFromSpotify(uri, MainActivity.getThis){
            @Override
            public void atLast() {
                if (activity!=null)
                loadingdialog.dismiss();
                SpotifyFragment.playSpotify();
                SpotifyPlaylistFragment.gettingList=false;
            }
        }.run();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //DebugLog.log("in fragment");
        if (requestCode == STATIC_RESULT_SELECT) //check if the request code is the one you've sent
        {
            //DebugLog.log("result ok");
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
        private FragmentActivity activity;
        private LinearLayout ll;

        public FavoritesListItem(SelectFragment selectFragment, View parentView, String listDescription, String selectlistViewcode,boolean visible) {
            this.selectlistViewcode=selectlistViewcode;


             activity = selectFragment.getActivity();
            favoriteTextView = new TextView(activity);
            favoriteTextView.setText(listDescription);

            this.visible=visible;
            ll = new LinearLayout(activity);
            setLayoutParams();

            final float scale = getResources().getDisplayMetrics().density;
            int dpHeightInPx = (int) (40 * scale);
            favoriteTextView.setMinHeight(dpHeightInPx);

            favoriteTextView.setGravity(Gravity.CENTER_VERTICAL);
            favoriteTextView.setBackgroundColor(Color.parseColor("#4A9C67"));
            ll.addView(favoriteTextView);
            this.favoritesAdded = new ArrayList<>();
            this.favoritesAddedListView = new ListView(activity);
            this.favoritesAddedListAdapter = new FavoriteListAdapter(activity, selectFragment, true, this.favoritesAdded);
            this.favoritesAddedListView.setAdapter(favoritesAddedListAdapter);

            ll.addView(this.favoritesAddedListView);
            LinearLayout rl=((LinearLayout) parentView.findViewById(R.id.favoritesLinearLayout));
            rl.addView(ll);

        }

        public void setLayoutParams() {
            setDistance(2);
        }

        public void setDistance(int distance) {
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setPadding(distance, distance, distance, distance);

            //LL.setPadding(10, 10, 10, 10);
            ll.setLayoutParams(LLParams);

            LLParams.setMargins(0, distance, distance, 0);
            favoriteTextView.setLayoutParams(LLParams);
        }

        public FavoritesListItem(SelectFragment selectFragment, View parentView, String listDescription, String selectlistViewcode) {
            this( selectFragment,  parentView,  listDescription,  selectlistViewcode,true);
        }
        public void toggleVisible(){
            visible=!visible;
            setLayoutParams();
        }
        public boolean isVisible(){
            return visible;
        }
    }
}
