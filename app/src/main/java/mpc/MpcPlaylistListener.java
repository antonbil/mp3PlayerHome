package mpc;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;

/**
 * Created by anton on 31-1-16.
 */
public interface MpcPlaylistListener {
    void databaseCallCompleted(ArrayList<String> files);
}
