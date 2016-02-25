package examples.quickprogrammingtips.com.tablayout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by anton on 24-2-16.
 */
public class MainScreenDialog extends Dialog  {
    Dialog th;
    Timer timer;
    TextView time;
    TextView totaltime;
    MainActivity getThis;
    public MainScreenDialog(Context context) {
        super(context);
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
                timer.cancel();
                th.dismiss();
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {

                    //getThis.albumBitmap;

                    getThis.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.ViewHolder vh = getThis.viewHolder;
                            ImageView image = (ImageView) findViewById(R.id.mainscreenthumbnail_top);
                            image.setImageBitmap(getThis.albumBitmap);
                            TextView tvName = (TextView) findViewById(R.id.mainscreentitle_top);

                            tvName.setText(vh.title);

                            time.setText(vh.time);
                            Log.v("samba", vh.time);
                            totaltime.setText(vh.totaltime);
                            TextView artist = (TextView) findViewById(R.id.mainscreenartist_top);
                            artist.setText(vh.album);
                        }
                    });

                } catch (Exception e) {

                }
            }


        }, 0, 1000);//Update text every second

        /*yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);*/

    }



}
