package redmaple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import org.cmc.music.common.ID3ReadException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import redmaple.songfinder.Songfinder;
import redmaple.songfinder.StoredMusic;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class DesktopSongFinder implements Songfinder {
    private RedMaple rm;

    public DesktopSongFinder(RedMaple rm) {

        this.rm = rm;
    }

    @Override
    public void printSongs() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FileHandle openNativeFileSelector() {

        if (true)
            return null;

        JFileChooser fc = new JFileChooser();

        fc.showDialog(null, "Select");

        File sel = fc.getSelectedFile();
        if (sel == null) return null;
        return Gdx.files.absolute(sel.getAbsolutePath());  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static final String[] musicFileEndings = {".mp3", ".ogg", ".wav"};

    private static boolean isMusicFile(FileHandle fh) {
        for (String s : musicFileEndings)
            if (fh.name().toLowerCase().endsWith(s))
                return true;
        return false;
    }

    private void recursivelyGetMusic(FileHandle folder, Array<StoredMusic> arr) {
        for (FileHandle fh : folder.list()) {
            if (fh.isDirectory())
                recursivelyGetMusic(fh, arr);
            else if (isMusicFile(fh)) {

                /*
                // This library is beyond stupid but the only one with sane api. Oh well
                MusicMetadataSet srcSet = null;
                try {
                    srcSet = new MyID3().read(fh.file());
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    continue;
                }
                String artist, album, title;
                if (srcSet != null) {
                    IMusicMetadata metadata = srcSet.getSimplified();
                    artist = metadata.getArtist();
                    album = metadata.getAlbum();
                    title = metadata.getSongTitle();
                }
                else {
                    artist = "Unknown";
                    album = "Unknown";
                    title = "Unknown"; // TODO compute from filename
                }
                */

                arr.add(new StoredMusic(
                        0,
                        "",
                        "",
                        fh.name(),
                        fh
                ));
            }
        }
    }

    @Override
    public StoredMusic[] getStoredMusic() {

        Array<StoredMusic> musix = new Array<StoredMusic>();

        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {

            FileHandle musicLibrary = Gdx.files.absolute("C:/Users/" + System.getProperty("user.name") + "/AppData/Roaming/Microsoft/Windows/Libraries/Music.library-ms");
            if (musicLibrary.exists()) {
                XmlReader reader = new XmlReader();
                XmlReader.Element el = null;
                try {
                    el = reader.parse(musicLibrary);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (el != null) {

                    el = el.getChildByName("searchConnectorDescriptionList");
                    Array<XmlReader.Element> libraryDescs = el.getChildrenByName("searchConnectorDescription");

                    for (XmlReader.Element libraryElement : libraryDescs) {
                        String url = libraryElement.getChildByName("simpleLocation").get("url");
                        if (url.contains("{")) // One of those knownFolder things. Idk
                            continue;
                        FileHandle location = Gdx.files.absolute(url);
                        if (location.exists())
                            recursivelyGetMusic(location, musix);
                        else
                            System.out.println("Warning: Music lib " + url + " doesn't exist");
                    }
                }
            }

        }

        // Get music in current folder
        recursivelyGetMusic(Gdx.files.absolute(new File(".").getAbsolutePath()), musix);

        return musix.toArray(StoredMusic.class);


        // TODO make work


        /*
        FileHandle[] files = Gdx.files.absolute("X:/Downloads/Daft Punk Complete Discography 2011/2001 - Discovery/").list("MP3");
        StoredMusic[] sm = new StoredMusic[files.length];

        int i = 0;
        for (FileHandle list : files) {
            try {
                // This library is beyond stupid but the only one with sane api. Oh well
                MusicMetadataSet srcSet = new MyID3().read(list.file());
                String artist, album, title;
                if (srcSet != null) {
                    IMusicMetadata metadata = srcSet.getSimplified();
                    artist = metadata.getArtist();
                    album = metadata.getAlbum();
                    title = metadata.getSongTitle();
                }
                else {
                    artist = "Unknown";
                    album = "Unknown";
                    title = "Unknown"; // TODO compute from filename
                }

                sm[i++] = new StoredMusic(0, artist, album, title, list);

            } catch (IOException e) {
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ID3ReadException e) {
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return sm;

        */
        /*

        return new StoredMusic[] {
                new StoredMusic(
                        0,
                        "Mat Zo & Porter Robinsson",
                        "idk",
                        "Easy",
                        Gdx.files.internal("snd/easy_lowquality.ogg")
                )
        };
        */
    }


}
