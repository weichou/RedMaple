package redmaple.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.4.2013
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class ColorUtils {
    public static float[] createArray(int len) {

        final float freq1 = .3f;
        final float freq2 = .3f;
        final float freq3 = .3f;
        final int phase1 = 0;
        final int phase2 = 2;
        final int phase3 = 4;

        float[] ret = new float[len];

        for (int i = 0;i < len; i++) {
            float red = MathUtils.sin(freq1*i + phase1);
            float green = MathUtils.sin(freq2*i + phase2);
            float blue = MathUtils.sin(freq3*i + phase3);

            red = (red + MathUtils.random(1.0f)) / 2;
            green = (green + MathUtils.random(1.0f)) / 2;
            blue = (blue + MathUtils.random(1.0f)) / 2;

            ret[i] = new Color(red, green, blue, 1).toFloatBits();
        }

        return ret;
    }
}
