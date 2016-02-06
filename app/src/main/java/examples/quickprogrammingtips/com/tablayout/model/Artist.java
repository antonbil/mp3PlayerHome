package examples.quickprogrammingtips.com.tablayout.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by anton on 8-1-16.
 * An Artist has Albums
 */
public class Artist extends Performer {
    //fields
    private static ArrayList<Artist>artists;
    private String country;

    /**
     *
     * @param name: name of artist
     * @param country: country of artist
     */
    public Artist(String name, String country){
        this.setName(name);
        this.setCountry(country);
    }

    /**
     *
     * @param name: name of artist
     */
    public Artist(String name){
        this.setName(name);
    }

    /**
     * list all artists
     * @return a list of all artists
     */
    public static ArrayList<String> listArtists(){
        ArrayList<String> returnValue=new ArrayList<String>();
        for (int i=0;i<artists.size();i++){
            //getArtist is created using refactoring
            String s = getArtist(i);
            returnValue.add(s);
        }
        return returnValue;
    }

    @NonNull
    private static String getArtist(int i) {
        Artist artist=artists.get(i);
        return artist.getName()+"-"+artist.getCountry();
    }

    /**
     * set list of all artists
     * @param artistsparameter: list of all artists
     */
    public static void setArtists(ArrayList<Artist>artistsparameter){
        artists=artistsparameter;
    }

    /**
     *
     * @return country of artist
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param country country of artist
     */
    public void setCountry(String country) {
        this.country = country;
    }
}
