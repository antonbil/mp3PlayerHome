package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.adapters.FavoriteListAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Server;


public class SelectFragment extends Fragment implements FavoritesInterface{
    static final int STATIC_RESULT=2; //positive > 0 integer.
    private Logic logic;
    ArrayList<Favorite> favorites;
    ListView favoriteListView;
    //ArrayList<Favorite> favoritesAdded = new ArrayList<>();;
    FavoriteListAdapter favoriteListAdapter;
     //ListView favoritesAddedListView;
     //FavoriteListAdapter favoritesAddedListAdapter;
    ArrayList<FavoritesListItem>favoritesListItemArray=new ArrayList<>();
    //FavoritesListItem favoritesListItem;

    private ArrayList<Server>servers=Server.servers;//Server.servers.get(Server.getServer(getActivity())).url;
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
                            //Toast.makeText(getActivity(), "choice: "+servers.get(i).description,

                            //Toast.LENGTH_SHORT).show();
                            Server.setServer(i, getActivity());
                        }
                    }

                }

            });

            favoriteListView = (android.widget.ListView) view.findViewById(R.id.selectlistView);
            favoriteListAdapter = new FavoriteListAdapter(getActivity(), this, false,favorites);
            favoriteListView.setAdapter(favoriteListAdapter);

            for (int i=0;i<Favorite.categoryIds.size();i++)
            favoritesListItemArray.add(new FavoritesListItem(this, view, Favorite.getCategory(i), Favorite.categoryIds.get(i)));
            //favoritesListItem.favoritesAddedListView= (android.widget.ListView) view.findViewById(R.id.selectlist1View);
            //favoritesAddedListAdapter = new FavoriteListAdapter(getActivity(), this, true, favoritesAdded);
            //favoritesAddedListView.setAdapter(favoritesAddedListAdapter);

            getFavorites();
            setTest(view);
            return view;
        }

    private void setTest(View view){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


            }
        });

    }
    @NonNull
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
                Log.v("samba", favItem.selectlistViewcode + " vs " + fav.category);
                if (favItem.selectlistViewcode.equals(fav.category))
                    favItem.favoritesAdded.add(favnew);
            }

        }
        for (FavoritesListItem fi:favoritesListItemArray)
            if(fi.favoritesAdded.size()>0)fi.favoriteTextView.setVisibility(View.VISIBLE); else fi.favoriteTextView.setVisibility(View.GONE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                setHeightListView(favoriteListAdapter, favoriteListView,favorites);
                for (FavoritesListItem fi:favoritesListItemArray) {
                    Collections.sort(fi.favoritesAdded, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Favorite mp1=(Favorite)o1;
                            Favorite mp2=(Favorite)o2;
                            return mp1.getDescription().compareTo(mp2.getDescription());
                        }
                    });
                    setHeightListView(fi.favoritesAddedListAdapter, fi.favoritesAddedListView, fi.favoritesAdded);
                }


            }
        });

    }

    public void setHeightListView(FavoriteListAdapter nyAdapter, ListView myListView, ArrayList<Favorite> favorites) {
        nyAdapter.notifyDataSetChanged();

        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = 130* favorites.size();
        myListView.setLayoutParams(params);
        myListView.requestLayout();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.selectlistView) {
        }
    }
    public void setAddress(String address) {
        logic.openServer(address);
        logic.getMpc().setMPCListener((MainActivity) getActivity());
    }

    @Override
    public void favoritesCall(Favorite favorite, String id) {
        if (id=="edit"){
            if (favorite.getRecord()!=null) {
                Intent intent = new Intent(getActivity(), EditFavoriteActivity.class);
                intent.putExtra("id", (int)(favorite.getRecord().getId()+0));
                intent.putExtra("url", favorite.getRecord().url);
                intent.putExtra("description", favorite.getDescription());
                intent.putExtra("category", favorite.getCategory());
                startActivityForResult(intent, STATIC_RESULT);
            }


        } else
        if (id=="delete"){
            if (favorite.getRecord()!=null) {

                FavoriteRecord book = FavoriteRecord.findById(FavoriteRecord.class, favorite.getRecord().getId());
                book.delete();
                getFavorites();
            }

        } else {
            String uri = favorite.getUri();
            if (uri.startsWith("smb://")) {
                logic.getHistory().add(uri);
                ((MainActivity) getActivity()).selectTab(1);
            }else{
                logic.getHistoryMpd().add(uri);
                ((MainActivity) getActivity()).selectTab(2);
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


                } else {
                // the result code is different from the one you've finished with, do something else.
            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }

    private class FavoritesListItem {
        private final View view;
        public  String selectlistViewcode;
        public  ListView favoritesAddedListView;
        public  FavoriteListAdapter favoritesAddedListAdapter;
        private String listDescription;
        public ArrayList<Favorite> favoritesAdded;
        public TextView favoriteTextView;

        public FavoritesListItem(SelectFragment selectFragment, View view, String selectlist1View, String selectlistViewcode) {
            this.view=view;
            this.listDescription =selectlist1View;
            this.selectlistViewcode=selectlistViewcode;
            /*
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="10dip"
                    android:gravity="center_vertical"

             */
            LinearLayout LL = new LinearLayout(selectFragment.getActivity());
            //LL.setBackgroundColor(Color.CYAN);
            LL.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

            LL.setPadding(10, 10, 10, 10);
            LL.setLayoutParams(LLParams);
            favoriteTextView = new TextView(selectFragment.getActivity());
            favoriteTextView.setText(selectlist1View);
            LLParams.setMargins(0, 10, 0, 0);
            favoriteTextView.setLayoutParams(LLParams);
            favoriteTextView.setGravity(Gravity.CENTER_VERTICAL);
            favoriteTextView.setBackgroundColor(Color.parseColor("#4A9C67"));
            LL.addView(favoriteTextView);
            this.favoritesAdded = new ArrayList<>();
            //this.favoritesAdded.add(new Favorite("smb://192.168.2.8/FamilyLibrary/years/2014/", "2014"));
            this.favoritesAddedListView = new ListView(selectFragment.getActivity());
            this.favoritesAddedListAdapter = new FavoriteListAdapter(selectFragment.getActivity(), selectFragment, true, this.favoritesAdded);
            this.favoritesAddedListView.setAdapter(favoritesAddedListAdapter);
            //favoriteTextView.setText(selectlist1View);
            LL.addView(this.favoritesAddedListView);
            LinearLayout rl=((LinearLayout) view.findViewById(R.id.favoritesLinearLayout));
            rl.addView(LL);
        }
    }
}
