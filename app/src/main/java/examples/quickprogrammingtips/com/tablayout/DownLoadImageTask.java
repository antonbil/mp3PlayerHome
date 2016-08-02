package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public abstract class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
    public static HashMap<String, Bitmap> albumPictures=new HashMap<>();


    public DownLoadImageTask() {
    }

    public abstract void setImage(Bitmap logo);

    /*
        doInBackground(Params... params)
            Override this method to perform a computation on a background thread.
     */
    protected Bitmap doInBackground(String... urls) {
        String urlOfImage = urls[0];
        Bitmap logo = null;
        if (albumPictures.containsKey(urlOfImage)) {
            //if (albumPictures.get(niceAlbumName) != null)

            try {
                int n=0;
                while ((albumPictures.get(urlOfImage) == null)&&(n<30)) {
                    //Log.v("samba","wait....."+n+" iteration");
                    Thread.sleep(1000);
                    n++;
                }
                return(albumPictures.get(urlOfImage));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //setBitmap(albumPictures.get(niceAlbumName));

        } else             try {
            InputStream is = new URL(urlOfImage.replace(" ","%20")).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
            logo = BitmapFactory.decodeStream(is);
            albumPictures.put(urlOfImage, logo);//so image is loaded only once
        } catch (Exception e) { // Catch the download exception
            //albumPictures.clear();
            //Log.v("samba", Log.getStackTraceString(e));
        }
        return logo;
    }

    /*
        onPostExecute(Result result)
            Runs on the UI thread after doInBackground(Params...).
     */
    protected void onPostExecute(Bitmap result) {
        setImage(result);
        //imageView.setImageBitmap(result);
    }
}
