package redmaple.android.RMAndroid;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.badlogic.gdx.Gdx;
import redmaple.sql.ActionResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AndroidActionResolver implements ActionResolver {

    Handler uiThread;
    Context appContext;

    public AndroidActionResolver(Context appContext) {
            uiThread = new Handler();
            this.appContext = appContext;
    }

    @Override
    public Connection getConnection() {
        String file = appContext.getFilesDir().getAbsolutePath() + "mapcache.sqlite";
        Gdx.app.log("RedMaple", "Db folder: " + file);
        String url = "jdbc:sqldroid:" + file;
        try {
                Class.forName("org.sqldroid.SQLDroidDriver").newInstance();
                return DriverManager.getConnection(url);
        } catch (InstantiationException e) {
                Log.e("RedMaple", e.getMessage());
        } catch (IllegalAccessException e) {
                Log.e("RedMaple", e.getMessage());
        } catch (ClassNotFoundException e) {
                Log.e("RedMaple", e.getMessage());
        } catch (SQLException e) {
                Log.e("RedMaple", e.getMessage());
        }
        return null;
    }
}