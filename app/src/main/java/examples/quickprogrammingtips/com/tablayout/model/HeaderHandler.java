package examples.quickprogrammingtips.com.tablayout.model;

import java.util.ArrayList;
import java.util.Iterator;

import examples.quickprogrammingtips.com.tablayout.HeaderSongInterface;

/**
 * Created by anton on 13-10-16.
 */

public class HeaderHandler  implements Iterable<HeaderSongInterface>{
    private ArrayList<HeaderSongInterface> headers;

    public HeaderHandler() {
        headers = new ArrayList<HeaderSongInterface>();
    }

    public void add(HeaderSongInterface game) {
        headers.add(game);
    }

    @Override
    public Iterator<HeaderSongInterface> iterator() {

        return headers.iterator();
    }

    public void removeItem(HeaderSongInterface h){
        for (int i=headers.size()-1;i>=0;i--){
            if (headers.get(i).equals(h)) {
                headers.remove(i);
                break;
            }
        }

    }
}

