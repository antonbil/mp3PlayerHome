package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;

/**
 * Created by anton on 23-1-16.
 */
public interface SambaInterface {
    void sambaCallCompleted(ArrayList<File> files, ArrayList<File> filesMp3, String id);
    void newSambaCall(String path, String id);
}
