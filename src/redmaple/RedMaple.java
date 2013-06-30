package redmaple;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import redmaple.game.GameScreen;
import redmaple.menu.MenuScreen;
import redmaple.songfinder.Songfinder;
import redmaple.sql.ActionResolver;
import redmaple.util.perf.PerfFuncs;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class RedMaple extends Game {

    public static Songfinder songfinder;
    public static ActionResolver actionResolver;
    public static PerfFuncs perfFuncs;

    public GameScreen gameScreen;
    public MenuScreen menuScreen;

    public Connection dbConnection;

    @Override
    public void create() {

        Gdx.app.log("RedMaple", "PerfFuncs n:" + perfFuncs.isNative());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dbConnection = actionResolver.getConnection();

                    if (dbConnection != null) {

                        Statement stmt = dbConnection.createStatement();
                        stmt.setQueryTimeout(5); // Shouldn't take long

                        int i = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS maps (id INTEGER PRIMARY KEY, name STRING UNIQUE, json STRING)");

                        Gdx.app.log("RedMaple", "Connected to database! CREATE TABLE updates: " + i);

                    }
                }
                catch (Exception e) {
                    Gdx.app.error("RedMaple", "Failed to connect to database!", e);
                }
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        gameScreen = new GameScreen(this);
        menuScreen = new MenuScreen(this);

        setScreen(menuScreen);
    }

    public void switchTo(GScreen gScreen) {
        switch (gScreen) {
            case Game:
                setScreen(gameScreen);
                break;
            case Menu:
                setScreen(menuScreen);
                break;
        }
    }

    @Override
    public void dispose() {
        gameScreen.dispose();
        menuScreen.dispose();

        perfFuncs.dispose();

        if (dbConnection != null)
            try {
                dbConnection.close();
                Gdx.app.log("RedMaple", "DbConnection closed");
            } catch (SQLException e) {
                Gdx.app.error("RedMaple", "Failed to close dbConnection", e);
            }
    }

    public String saveMap(String mapName, String json) {

        if (dbConnection == null) return "dbConnection null";

        try {
            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO maps (name, json) VALUES (?, ?)");

            pstmt.setString(1, mapName);
            pstmt.setString(2, json);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            return e.toString();
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public String getMapJson(String mapName) {

        if (dbConnection == null) return null;

        try {
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT json FROM maps WHERE name = ?");
            pstmt.setString(1, mapName);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) // First lol
                return result.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public boolean isCached(String cachedLowerToString) {
        return getMapJson(cachedLowerToString) != null;
    }

    public enum GScreen {
        Game, Menu
    }
}
