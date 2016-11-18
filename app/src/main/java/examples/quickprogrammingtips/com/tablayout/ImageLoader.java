package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 12-11-16.
 * copied from http://www.androidhive.info/2012/07/android-loading-image-from-url-http
 * added filecaching limited to last two days
 * minor changes to adopt it to my own application
 */


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class ImageLoader {

    private static final int DAYSTOSAVE = 2;//files older than two days are automatically deleted

    MemoryCache memoryCache=new MemoryCache();
    private FileCache fileCache;
    private Activity activity;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService executorService;

    private static void checkAllFilesInDirIfTheyCanBeOmittedIfTooOld(File dir) {
        if (dir == null)
            return;

        Stack<File> dirlist = new Stack<>();
        dirlist.clear();
        dirlist.push(dir);

        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory())
                    dirlist.push(aFileList);
                else{
                    fileDeleteIfOlder(aFileList);
                }
            }
        }
    }

    private static void checkOlderFiles(){
        File f = FileCache.getCacheDir();
        checkAllFilesInDirIfTheyCanBeOmittedIfTooOld(f);
    }
    private static void fileDeleteIfOlder(File file){
        if(file.exists()){
            Calendar time = Calendar.getInstance();
            time.add(Calendar.DAY_OF_YEAR, -DAYSTOSAVE);//mind the minus; substract number of days
            //I store the required attributes here and delete them
            Date lastModified = new Date(file.lastModified());
            if(lastModified.before(time.getTime()))
            {
                file.delete();
                DebugLog.log("delete file:"+file.getName());
            }
        }
    }


    ImageLoader(Context context){
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
        new Thread(ImageLoader::checkOlderFiles).start();
    }

    private final int stub_id=R.drawable.common_full_open_on_phone;
    void DisplayImage(String url, DoAction doAction)
    {
        DisplayImage(url,null,doAction);

    }
    public void DisplayImage(String url, ImageView imageView, DoAction doAction)
    {
        try{
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null){
            setImage(imageView, bitmap);
            doAction.doAction(bitmap);
        }
        else
        {
            queuePhoto(url, imageView,doAction);
            if (imageView!=null)
            imageView.setImageResource(stub_id);
        }
    } catch (Exception e) {            Log.v("samba", Log.getStackTraceString(e));        }

    }

    private void queuePhoto(String url, ImageView imageView, DoAction doAction)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView,doAction);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url)
    {
        File f=fileCache.getFile(url);

//from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;

//from web
        try{
        try {
            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
            ex.printStackTrace();
            if(ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    } catch (Exception e) {            Log.v("samba", Log.getStackTraceString(e));
            return null;
        }

}

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
//decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

//Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

//decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (Exception e) { /**/}
        return null;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    //Task for the queue
    private class PhotoToLoad
    {
        private final DoAction doAction;
        public String url;
        ImageView imageView;
        PhotoToLoad(String u, ImageView i, DoAction doAction){
            url=u;
            this.doAction=doAction;
            imageView=i;
        }
    }

    private class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }

        @Override
        public void run() {
            try{
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url);
                if (bmp==null)return;
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad)){
                photoToLoad.doAction.doAction(bmp);
                return;
            }
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                //next command was originally in library. It gives error if in Thread
            //(Activity)photoToLoad.imageView.getContext();
            activity.runOnUiThread(bd);
                photoToLoad.doAction.doAction(bmp);
        } catch (Exception e) {            Log.v("samba", Log.getStackTraceString(e));        }
        }
    }

    private boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    //Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            try{
            if(imageViewReused(photoToLoad)){
                return;
            }
                setImage(photoToLoad.imageView,bitmap);
            } catch (Exception e) {            Log.v("samba", Log.getStackTraceString(e));        }
        }

    }

    private void setImage(ImageView imageView, Bitmap bitmap) {
        if(bitmap !=null){
            if (imageView!=null)
            imageView.setImageBitmap(bitmap);

        }
        else
            if (imageView!=null)
                imageView.setImageResource(stub_id);
    }
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
}
