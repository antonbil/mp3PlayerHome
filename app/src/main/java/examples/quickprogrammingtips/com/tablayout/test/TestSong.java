package examples.quickprogrammingtips.com.tablayout.test;

/**
 * Created by anton on 20-1-16.
 */
import junit.framework.Assert;
import junit.framework.TestCase;

import examples.quickprogrammingtips.com.tablayout.model.Song;

public class TestSong extends TestCase {

    String title = "Title test";
    Song song = new Song(title);//created new constructor; test it!


    /*
    check if title is set correctly
     */
    public void testTitle() {


        Assert.assertEquals(song.getTitle(), title);


    }
}