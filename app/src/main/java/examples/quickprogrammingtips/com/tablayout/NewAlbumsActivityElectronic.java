package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public     class NewAlbumsActivityElectronic extends NewAlbumsActivity {
    protected String url;
        /*
        //programmatic way:
//doc.select("div").first();//giving an Element instance
         */

    public void setUrl(){
        url="http://www.spotifynewmusic.com/tagwall3.php?ans=electronic";
    }

    @Override
    public void generateList(ArrayList<NewAlbum> newAlbums) {
        Bundle extras = getIntent().getExtras();


        url= extras.getString("url");
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            String temp = doc.html().replace("<br>", "$$$").replace("<br />", "$$$"); //$$$ instead <br>
            doc = Jsoup.parse(temp); //Parse again
        } catch (IOException e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

        Elements trackelements = doc.getElementsByClass("album");
        //;
        //ArrayList<String> ids = new ArrayList<String>();
        for (Element element : trackelements) {
            String image="http://www.spotifynewmusic.com/"+element.select("img").attr("src");//http://www.spotifynewmusic.com/covers/13903.jpg
            Elements links = element.getElementsByClass("play").select("a[href]"); // a with href
            String s = links.get(0).attr("href");
            Log.v("samba",s);

            String div = element.children().get(1).text();
            Log.v("samba",div);
            try{
            String[] list = div.replace("$$$",";").split(";");
            String artist = list[0];
                String album = "";
                if (list.length>1)
            album = list[1];
            //ids.add(artist + "-" + album);
            newAlbums.add(new NewAlbum(s, artist, album,image));
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }

    }
}

