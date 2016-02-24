package examples.quickprogrammingtips.com.tablayout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by anton on 24-2-16.
 */
public class MainScreenDialog extends Dialog implements
        android.view.View.OnClickListener {
    public MainScreenDialog(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainscreen_dialog);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    final MainActivity getThis = MainActivity.getThis;
                    //getThis.albumBitmap;

                    getThis.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.ViewHolder vh=getThis.viewHolder;
                            ImageView image = (ImageView) findViewById(R.id.mainscreenthumbnail_top);
                            image.setImageBitmap(getThis.albumBitmap);
                            TextView tvName = (TextView) findViewById(R.id.mainscreentitle_top);
                            tvName.setText(vh.title);
                            TextView time = (TextView) findViewById(R.id.mainscreentime_top);
                            time.setText(vh.time);
                            TextView totaltime = (TextView) findViewById(R.id.mainscreentotaltime_top);
                            totaltime.setText(vh.totaltime);
                            TextView artist = (TextView) findViewById(R.id.mainscreenartist_top);
                            artist.setText(vh.album);                        }
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

    @Override
    public void onClick(View v) {

    }
}
