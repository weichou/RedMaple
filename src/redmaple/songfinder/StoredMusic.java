package redmaple.songfinder;

import com.badlogic.gdx.files.FileHandle;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 19:26
 * To change this template use File | Settings | File Templates.
 */
public class StoredMusic {
    public StoredMusic[] children;

    public int id;
    public String artist;
    public String name;
    public String album;
    public FileHandle fileHandle;

    private final String cachedToString;
    public final String cachedLowerToString;

    public StoredMusic(int id, String artist, String album, String name, FileHandle absolute) {
        this.id = id;
        this.artist = artist;
        this.name = name;
        this.album = album;
        this.fileHandle = absolute;

        this.cachedToString = artist + " - " + name;
        this.cachedLowerToString = cachedToString.toLowerCase();
    }

    @Override
    public String toString() {
        return cachedToString;
    }
}
