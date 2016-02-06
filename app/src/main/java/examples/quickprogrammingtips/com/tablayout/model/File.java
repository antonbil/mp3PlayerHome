package examples.quickprogrammingtips.com.tablayout.model;

/**
 * Created by anton on 23-1-16.
 */
public class File {
    private String fname;
    private String path;

    public File(){

    }

    public File(String fname, String path){
        this.fname=fname;
        this.path=path;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public String toString(){
        return path+fname;
    }
}
