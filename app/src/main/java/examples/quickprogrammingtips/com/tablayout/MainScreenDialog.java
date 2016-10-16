package examples.quickprogrammingtips.com.tablayout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by anton on 24-2-16.
 */
public class MainScreenDialog extends Dialog implements HeaderSongInterface {
    Dialog th;
    TextView time;
    TextView totaltime;
    MainActivity getThis;
    private ImageView image;
    private TextView tvName;
    private TextView artist;

    public MainScreenDialog(Context context) {
        super(context);
    }
    @Override
    public void onStop() {
        try{
            MainActivity.headers.removeItem(this);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getThis = MainActivity.getThis;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainscreen_dialog);
        Button b=(Button) findViewById(R.id.okbutton);
        time = (TextView) findViewById(R.id.mainscreentime_top);
        totaltime = (TextView) findViewById(R.id.mainscreentotaltime_top);

        Typeface face= Typeface.createFromAsset(getThis.getAssets(), "fonts/DS-DIGIT.ttf");
        time.setTypeface(face);
        totaltime.setTypeface(face);

        th=this;
        setCancelable(false);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                th.dismiss();
            }
        });
        image = (ImageView) findViewById(R.id.mainscreenthumbnail_top);
        tvName = (TextView) findViewById(R.id.mainscreentitle_top);
        artist = (TextView) findViewById(R.id.mainscreenartist_top);

        MainActivity.headers.add(this);

    }


    @Override
    public void setLogo(Bitmap logo) {
        image.setImageBitmap(logo);
    }

    @Override
    public void setData(String time1, String totalTime, String title, String artist1, boolean spotifyList, int currentTrack) {
        tvName.setText(title);

        time.setText(time1);
        totaltime.setText(totalTime);
        artist.setText(artist1);
    }
}
