package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

abstract class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
    private static HashMap<String, Bitmap> albumPictures=new HashMap<>();//DownLoadImageTask.albumPictures=new HashMap<>();


    DownLoadImageTask() {
    }

    public abstract void setImage(Bitmap logo);

    /*
        doInBackground(Params... params)
            Override this method to perform a computation on a background thread.
     */
    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
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
            //if (logo.getHeight()>=250)
            logo = BitmapFactory.decodeStream(is);
            if (logo.getHeight()>250)
            logo=getResizedBitmap(logo,250,250);
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
