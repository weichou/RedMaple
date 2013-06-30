package redmaple.android.RMAndroid;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.SimpleCursorAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import redmaple.RedMaple;
import redmaple.songfinder.Songfinder;
import redmaple.songfinder.StoredMusic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class AndroidSongFinder implements Songfinder {
    private RedMapleActivity redMapleActivity;
    private RedMaple redMaple;

    public AndroidSongFinder(RedMapleActivity redMapleActivity, RedMaple redMaple) {

        this.redMapleActivity = redMapleActivity;
        this.redMaple = redMaple;
    }

    @Override
    public void printSongs() {
        /*
        */
    }

    @Override
    public FileHandle openNativeFileSelector() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StoredMusic[] getStoredMusic() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.MIME_TYPE + " == 'audio/mpeg'";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = redMapleActivity.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        Array<StoredMusic> sm = new Array<StoredMusic>();

        while(cursor.moveToNext()){

            FileHandle fh = Gdx.files.absolute(cursor.getString(3));
            if (!fh.exists()) {
                 continue;
            }

            sm.add(new StoredMusic(
                    cursor.getInt(0),
                    cursor.getString(1),
                    "-",
                    cursor.getString(2),
                    fh
            ));

            //Gdx.app.log("RedMaple", "Song found: " + cursor.getString(0) + " (" + cursor.getInt(0) + ")||" + cursor.getString(1) + "||" +   cursor.getString(2) + "||" +   cursor.getString(3) + "||" +  cursor.getString(4) + "||" +  cursor.getString(5));
        }

        return sm.toArray(StoredMusic.class); // ???
    }
}

