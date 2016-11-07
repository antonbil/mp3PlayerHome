package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

abstract class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
    private static HashMap<String, Bitmap> albumPictures=new HashMap<>();


    DownLoadImageTask() {
    }

    public abstract void setImage(Bitmap logo);

    Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight,boolean recycle) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;//Bitmap.Config.RGB_565
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        if (recycle)
        bm.recycle();
        return resizedBitmap;
    }
    protected Bitmap doInBackground(String... urls) {
        String urlOfImage = urls[0];
        Bitmap logo = null;
        if (albumPictures.containsKey(urlOfImage)) {

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


        } else             try {
            InputStream is = new URL(urlOfImage.replace(" ","%20")).openStream();

            logo = BitmapFactory.decodeStream(is);
            if (logo.getHeight()>300)
            logo=getResizedBitmap(logo,300,300,true);
            albumPictures.put(urlOfImage, logo);//so image is loaded only once
        } catch (Exception e) { // Catch the download exception

        }
        return logo;
    }

    protected void onPostExecute(Bitmap result) {
        setImage(result);
    }
}
