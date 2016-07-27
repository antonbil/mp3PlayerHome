package examples.quickprogrammingtips.com.tablayout;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SongItems  {
    private View.OnClickListener onClick;
    AppCompatActivity getThis;
    public TextView tvName;
    public  TextView time;
    public  TextView totaltime;
    public  TextView artist;
    public ImageView image;
    public SongItems(AppCompatActivity getThis){
        this.getThis=getThis;
        initSongItems();
    }

    public void checkTracks(String trackid){}

    private void initSongItems() {
        //Log.v("samba", "nosearch7");

        tvName = (TextView) getThis.findViewById(R.id.title_top);
        time = (TextView) getThis.findViewById(R.id.time_top);
        totaltime = (TextView) getThis.findViewById(R.id.totaltime_top);
        artist = (TextView) getThis.findViewById(R.id.artist_top);
        image = (ImageView) getThis.findViewById(R.id.thumbnail_top);



            /*Log.v("samba","nosearch8");
            ImageButton removeUpButton = (ImageButton) findViewById(R.id.removeupspotifyButton);
            removeUpButton.setOnClickListener(new View.OnClickListener()

            {
                //jsonrpc /jsonrpc?PlayerRemove {"jsonrpc": "2.0", "method": "Playlist.Remove", "params": { "playlistid": 0, "position": 0}, "id": 1}
                @Override
                public void onClick (View v){
                int counter = spotifyStartPosition;
                removeUplist(counter);
            }
            }

            );;*/

    }

    public View.OnClickListener getOnClick() {
        return onClick;
    }

    public void setOnClick(View.OnClickListener onClick) {
        this.onClick = onClick;
        image.setOnClickListener(onClick);
    }
}