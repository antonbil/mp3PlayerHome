package examples.quickprogrammingtips.com.tablayout.adapters;

/**
 * Created by anton on 20-1-16.
 */

        import android.content.Context;
        import android.support.v7.widget.PopupMenu;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ActionMenuView;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.ArrayList;

        import examples.quickprogrammingtips.com.tablayout.R;
        import examples.quickprogrammingtips.com.tablayout.model.Artist;

public class ArtistListAdapter extends BaseAdapter {

    private static ArrayList<Artist> artistList;
    private LayoutInflater mInflater;

    public ArtistListAdapter(Context artistFragmentContext, ArrayList<Artist> artists) {

        artistList = artists;

        mInflater = LayoutInflater.from(artistFragmentContext);
    }

    @Override
    public int getCount() {
        return artistList.size();
    }

    @Override
    public Artist getItem(int arg0) {
        return artistList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_performer, null);
            holder = new ViewHolder();
            holder.performer = (TextView) convertView.findViewById(R.id.textViewPerformer);
            holder.country = (TextView) convertView.findViewById(R.id.textViewPerformerCountry);
            holder.button = (Button) convertView.findViewById(R.id.optionbutton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.performer.setText(artistList.get(position).getName());
        holder.country.setText(artistList.get(position).getCountry());
        final int pos2=position;
            holder.button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.v("test", artistList.get(pos2).getName());
                    //Toast.makeText(v.getContext(), "click:" + (String) artistList.get(pos2).getName(), Toast.LENGTH_LONG).show();
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                    menu.getMenu().add("1");
                    menu.getMenu().add("a");
                    menu.getMenu().add("b");
                    menu.show();
                    final View v1=v;
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(v1.getContext(), "click:" +artistList.get(pos2).getName()+":"+ item.getTitle(), Toast.LENGTH_LONG).show();
                            return true;
                        }

                    });
                }
            });

        final String artistName = artistList.get(position).getName();
        Log.v("test", artistName);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast t = Toast.makeText(v.getContext(), artistName, Toast.LENGTH_SHORT);
                t.show();
            }
        });

        return convertView;
    }

    static class ViewHolder{
        TextView performer, country,button;
    }


}