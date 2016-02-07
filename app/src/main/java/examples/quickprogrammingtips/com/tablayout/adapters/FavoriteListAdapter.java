package examples.quickprogrammingtips.com.tablayout.adapters;

/**
 * Created by anton on 20-1-16.
 */

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.FavoritesInterface;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;

public class FavoriteListAdapter extends BaseAdapter {

    private boolean longClick=true;
    private ArrayList<Favorite> favoritesList;
    private LayoutInflater mInflater;
    FavoritesInterface fv;

    public FavoriteListAdapter(Context selectFragmentContext, FavoritesInterface fv, boolean longClick, ArrayList<Favorite> favorites) {

        favoritesList = favorites;
        this.longClick=longClick;


        mInflater = LayoutInflater.from(selectFragmentContext);
        this.fv=fv;
    }

    @Override
    public int getCount() {
        return favoritesList.size();
    }

    @Override
    public Favorite getItem(int arg0) {
        return favoritesList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_favorite, null);
            holder = new ViewHolder();
            //holder.url = (TextView) convertView.findViewById(R.id.textView_favoriteurl);
            holder.description = (TextView) convertView.findViewById(R.id.textView_favoritedescription);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.url.setText(favoritesList.get(position).getUri());
        holder.description.setText(favoritesList.get(position).getDescription());
        final int pos2=position;

        final String description = favoritesList.get(position).getDescription();

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast t = Toast.makeText(v.getContext(), description, Toast.LENGTH_SHORT);
                fv.favoritesCall(favoritesList.get(pos2), "select");
                //t.show();
            }
        });
        if (longClick)
        convertView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View v) {
                PopupMenu menu = new PopupMenu(v.getContext(), v);

                menu.getMenu().add("delete");
                menu.getMenu().add("edit");
                menu.getMenu().add("add to playlist");
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                            fv.favoritesCall(favoritesList.get(pos2), item.getTitle().

                                            toString()

                            );
                        //Toast.makeText(v.getContext(), "click:" + favoritesList.get(pos2).getDescription() + ":" + item.getTitle(), Toast.LENGTH_LONG).show();
                                return true;
                            }

                        });

                        return true;
                    }

                });

        return convertView;
    }

    static class ViewHolder{
        TextView description;
    }


}