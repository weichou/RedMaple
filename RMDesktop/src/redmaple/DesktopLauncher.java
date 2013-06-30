package redmaple;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        RedMaple rm = new RedMaple();
        RedMaple.songfinder = new DesktopSongFinder(rm);
        RedMaple.actionResolver = new DesktopActionResolver();
        RedMaple.perfFuncs = new DesktopPerfFuncs();

        File src = new File("gfxassets");
        File target = new File("../RMAndroid/assets/gfx");
        if (src.exists())
            TexturePacker2.process(src.getAbsolutePath(), target.getAbsolutePath(), "gfx.pack");

        new LwjglApplication(rm, "Red Maple", 800, 480, true);
    }
}
