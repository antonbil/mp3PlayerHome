package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

abstract class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
    //private static HashMap<String, Bitmap> albumPictures=new HashMap<>();


    DownLoadImageTask() {
    }

    public static MemoryCache getAlbumPictures() {
        return MainActivity.getInstance().imageLoader.memoryCache;
        //return albumPictures;
    }

    /*public static void setAlbumPictures(HashMap<String, Bitmap> albumPictures) {
        DownLoadImageTask.albumPictures = albumPictures;
    }*/

    public abstract void setImage(Bitmap logo);

    static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean recycle) {
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
        Bitmap logo = null;
        String urlOfImage = urls[0];
        if (urlOfImage.endsWith("original")){
            try {
                urlOfImage=urlOfImage.replace("original","");
                InputStream is = new URL(urlOfImage.replace(" ", "%20")).openStream();

                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception

            }
        } else {
            if (getAlbumPictures().get(urlOfImage)!=null) {

                try {
                    int n = 0;
                    while ((getAlbumPictures().get(urlOfImage) == null) && (n < 30)) {
                        //Log.v("samba","wait....."+n+" iteration");
                        Thread.sleep(1000);
                        n++;
                    }
                    return (getAlbumPictures().get(urlOfImage));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else try {
                //albumPictures.put(urlOfImage, null);//so image is loaded only once
                InputStream is = new URL(urlOfImage.replace(" ", "%20")).openStream();

                logo = BitmapFactory.decodeStream(is);
                logo=storeBitmapInCache(logo, urlOfImage, getAlbumPictures());
                //if (logo.getHeight() > 200)
                //    logo = getResizedBitmap(logo, 200, 200, true);
                //albumPictures.put(urlOfImage, logo);
            } catch (Exception e) { // Catch the download exception

            }
        }
        return logo;
    }
    public static Bitmap storeBitmapInCache(Bitmap logo, String urlOfImage,MemoryCache albumPictures) {
        logo = setBitmapsizeToDefault(logo);
        albumPictures.put(urlOfImage, logo);
        return logo;
    }

    public static Bitmap setBitmapsizeToDefault(Bitmap logo) {
        if (logo.getHeight() > 100)
            logo = getResizedBitmap(logo, 100, 100, true);
        return logo;
    }

    protected void onPostExecute(Bitmap result) {
        setImage(result);
    }
}
