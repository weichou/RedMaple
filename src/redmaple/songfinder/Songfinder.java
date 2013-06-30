package redmaple.songfinder;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public interface Songfinder {
    public void printSongs();
    public FileHandle openNativeFileSelector();

    StoredMusic[] getStoredMusic();
}
