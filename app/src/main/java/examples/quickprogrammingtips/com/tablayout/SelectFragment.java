package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
    static final int STATIC_RESULT=2; //positive > 0 integer.
    private Logic logic;
    ArrayList<Favorite> favorites;
    ListView favoriteListView;
    FavoriteListAdapter favoriteListAdapter;
    ArrayList<FavoritesListItem>favoritesListItemArray=new ArrayList<>();

    private ArrayList<Server>servers=Server.servers;//Server.servers.get(Server.getServer(getActivity())).url;
    private boolean regularFavoritesVisible=true;

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            servers.toArray();
            favorites = new ArrayList<>();

            //test records to db
            /*FavoriteRecord fv=new FavoriteRecord("abc", "abcpath", "2nd edition");
            fv.save();
            fv=new FavoriteRecord("abc2", "abcpath2", "2nd edition");
            fv.save();*/

            // Inflate the layout for this fragment
            logic =((MainActivity)getActivity()).getLogic();
            View view = inflater.inflate(R.layout.fragment_select, container, false);
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

                            Server.setServer(i, getActivity());
                        }
                    }

                }

            });

            TextView tv=(TextView)view.findViewById(R.id.favoriteTextlistView);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    regularFavoritesVisible=!regularFavoritesVisible;
                    getFavorites();                }
            });
            favoriteListView = (android.widget.ListView) view.findViewById(R.id.selectlistView);
            favoriteListAdapter = new FavoriteListAdapter(getActivity(), this, false,favorites);
            favoriteListView.setAdapter(favoriteListAdapter);

            for (int i=0;i<Favorite.categoryIds.size();i++) {
                final FavoritesListItem favoritesListItem = new FavoritesListItem(this, view, Favorite.getCategory(i), Favorite.categoryIds.get(i));
                favoritesListItem.favoriteTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoritesListItem.toggleVisible();
                        getFavorites();
                    }
                });
                favoritesListItemArray.add(favoritesListItem);
            }

            getFavorites();

            return view;
        }

    public void getFavorites() {

        favorites.clear();
        favorites.add(new Favorite("00tags/favorites", "favorites", ""));
        favorites.add(new Favorite("00tags/newest", "newest", ""));
        favorites.add(new Favorite("smb://192.168.2.8/FamilyLibrary/years/2014/", "2014", ""));
        favorites.add(new Favorite("smb://192.168.2.8/FamilyLibrary/years/2015/", "2015", ""));
        favorites.add(new Favorite("smb://192.168.2.8/FamilyLibrary/years/2016/", "2016", ""));
        favorites.add(new Favorite("smb://192.168.2.8/FamilyLibrary/years/", "years", ""));
        favorites.add(new Favorite("smb://192.168.2.8/FamilyLibrary/Soul/", "Soul", ""));

        List<FavoriteRecord> favoritesDisk = FavoriteRecord.listAll(FavoriteRecord.class);
        for (FavoritesListItem fi:favoritesListItemArray)
        fi.favoritesAdded.clear();
        for (FavoriteRecord fav:favoritesDisk){
            Favorite favnew=new Favorite(fav.url,fav.description,fav.category);
            favnew.setRecord(fav);
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
                if (regularFavoritesVisible)
                    Utils.setDynamicHeight(favoriteListView, 0);
                    //setHeightListView(favoriteListAdapter, favoriteListView,favorites);
                    else
                    setListViewHeight(favoriteListView, 0);
                for (FavoritesListItem fi:favoritesListItemArray) {
                    Collections.sort(fi.favoritesAdded, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Favorite mp1 = (Favorite) o1;
                            Favorite mp2 = (Favorite) o2;
                            return mp1.getDescription().compareTo(mp2.getDescription());
                        }
                    });
                    if (fi.isVisible())
                    //setHeightListView(fi.favoritesAddedListAdapter, fi.favoritesAddedListView, fi.favoritesAdded);
                    Utils.setDynamicHeight(fi.favoritesAddedListView, 0);
                    else setListViewHeight(fi.favoritesAddedListView, 0);
                }


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
        if (id.equals("edit")){
            if (favorite.getRecord()!=null) {
                Intent intent = new Intent(getActivity(), EditFavoriteActivity.class);
                intent.putExtra("id", (int)(favorite.getRecord().getId()+0));
                intent.putExtra("url", favorite.getRecord().url);
                intent.putExtra("description", favorite.getDescription());
                intent.putExtra("category", favorite.getCategory());
                startActivityForResult(intent, STATIC_RESULT);
            }


        } else
        if (id.equals("delete")){
            if (favorite.getRecord()!=null) {

                FavoriteRecord book = FavoriteRecord.findById(FavoriteRecord.class, favorite.getRecord().getId());
                book.delete();
                getFavorites();
            }

        } else {
            String uri = favorite.getUri();
            if (uri.startsWith("smb://")) {
                if (!id.equals("add to playlist"))
                {//todo add item add to playlist
                    Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
                    logic.getHistory().add(new HistoryListview(uri, 0));
                    ((MainActivity) getActivity()).selectTab(1);
                }
            }else{
                if (id.equals("add to playlist")){
                    String command=("add \"" + uri + "\"");
                    //Log.v("samba",command);
                    logic.getMpc().enqueCommands(new ArrayList<>(Collections.singletonList(command)));
                } else {
                    logic.getHistoryMpd().add(new HistoryListview(uri, 0));
                    ((MainActivity) getActivity()).selectTab(2);
                }
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == STATIC_RESULT) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // this is successful mission, do with it.
                favorites.clear();
                getFavorites();


                }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }

    private class FavoritesListItem {
        private boolean visible=true;
        public  String selectlistViewcode;
        public  ListView favoritesAddedListView;
        public  FavoriteListAdapter favoritesAddedListAdapter;
        public ArrayList<Favorite> favoritesAdded;
        public TextView favoriteTextView;

        public FavoritesListItem(SelectFragment selectFragment, View parentView, String listDescription, String selectlistViewcode) {
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
        }
        public void toggleVisible(){
            visible=!visible;
        }
        public boolean isVisible(){
            return visible;
        }
    }
}
