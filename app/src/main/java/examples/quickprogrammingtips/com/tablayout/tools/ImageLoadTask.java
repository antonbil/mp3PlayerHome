package examples.quickprogrammingtips.com.tablayout.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import examples.quickprogrammingtips.com.tablayout.MpdInterface;

/**
 * Created by anton on 25-1-16.
 * loads Image
 */
public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private MpdInterface imageView;
    private  ImageView image;
    private String album;

    public ImageLoadTask(String url, String album, MpdInterface imageView, ImageView image) {
        this.url = url;
        this.imageView = imageView;
        this.image=image;
        this.album=album;
    }

    public ImageLoadTask(String url, String album, MpdInterface imageView) {
        this.url = url;
        this.imageView = imageView;
        this.image=image;
        this.album=album;
    }
    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url.replace(" ", "%20"));
            //Log.v("samba","get:"+url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        //Log.v("samba", "set image");
        imageView.printCover(result,image,album);
    }

}