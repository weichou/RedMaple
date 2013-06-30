package redmaple;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 20:22
 * To change this template use File | Settings | File Templates.
 */
public class GameSettings {
    public static boolean Debug = Gdx.app.getType() == Application.ApplicationType.Desktop;
    public static boolean DebugGfx = Debug; // PRint debug data etc
}
