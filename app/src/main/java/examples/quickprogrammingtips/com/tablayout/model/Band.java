package examples.quickprogrammingtips.com.tablayout.model;

import java.util.ArrayList;

/**
 * Created by anton on 8-1-16.
 * A Band has one or more Albums
 */
public class Band extends Performer {
    //new attribute inclusding getter and setter
    private ArrayList<String> members;
    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public Band() {
    }

    /**
     *
     * @param name: name of band
     */
    public Band(String name){
        this.setName(name);
    }
}
