package examples.quickprogrammingtips.com.tablayout.model;

/**
 * Created by anton on 8-1-16.
 * A Performer has one or more ALbums
 */
public class Performer {
    protected String name;

    public Performer() {
    }

    /**
     *
     * @return name of band
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name: name of band
     */
    public void setName(String name) {
        this.name = name;
    }
}
