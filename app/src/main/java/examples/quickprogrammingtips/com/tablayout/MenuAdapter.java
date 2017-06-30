package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by anton on 11-10-16.
 */

public class MenuAdapter extends ArrayAdapter<String> {

    private final int itemHeight;
    private final int separatorHeight;

    public MenuAdapter(Context context, ArrayList<String> list) {
        super(context, 0, list);
        Display display = MainActivity.getInstance().getWindowManager().getDefaultDisplay();
// get size in pixels
        Point size = new Point();
        display.getSize(size);
        //int width = size.x;
        int height = size.y;
        itemHeight = (height/12);
        separatorHeight =itemHeight/8;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_menu_listview, parent, false);
        }
        // Lookup view for data population
        TextView listItem = (TextView) convertView.findViewById(R.id.list_content);
        listItem.setGravity(Gravity.CENTER_VERTICAL);
        listItem.setBackgroundColor(Color.WHITE);
        listItem.setTextColor(Color.BLACK);
        //if title is url, display only last part
        user=SpotifyFragment.getLastItem(user,"/");

        // Populate the data into the template view using the data object
        if (user.equals("sep")){
            user="";
            listItem.setBackgroundColor(Color.GREEN);
            listItem.setHeight(separatorHeight);
        }
        else {
            /*
                    itemHeight = (height/12);
        separatorHeight =itemHeight/8;

             */
            listItem.setHeight(itemHeight);
            user="   "+user;
        }
        listItem.setText(user);
        // Return the completed view to render on screen
        return convertView;
    }
}