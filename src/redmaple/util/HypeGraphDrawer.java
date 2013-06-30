package redmaple.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import redmaple.audio.AudioProcessor;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
public class HypeGraphDrawer {

    GameScreen screen;


    public HypeGraphDrawer(GameScreen screen) {
        this.screen = screen;
    }

    public void draw() {

        if (screen.mapGenerator == null)
            return;

        final ShapeRenderer sr = screen.shapeRenderer;

        float spectrumViewWidth = Gdx.graphics.getWidth();
        final float height = screen.camera.viewportHeight;

        sr.begin(ShapeRenderer.ShapeType.Line);

        {
            Vector2 lastHype = new Vector2();

            float xPerSpectrum = spectrumViewWidth / screen.audioProcessor.getSpectrumCount();

            final float drawYMax = height - 150;
            final float drawYMin = height - 250;

            final int curSpectrum = screen.audioProcessor.getLatencyAffectedSpectrumIndex();

            for (int i = 0; (i+10) < screen.audioProcessor.getSpectrumCount(); i+= 150) {
                float hype = screen.mapGenerator.hype(i, 150) * 100f;

                float drawX = xPerSpectrum * i;

                sr.setColor(0, 1, 0, 1f);
                sr.line(lastHype.x, drawYMin + lastHype.y, drawX, drawYMin + hype);

                lastHype.set(drawX, hype);
            }

            sr.setColor(1, 0, 0, 1);
            float lx = xPerSpectrum * curSpectrum;
            sr.line(lx, drawYMin, lx, drawYMax);

            sr.setColor(0, 0, 1, 1);
            lx = xPerSpectrum * screen.visibleTick;
            sr.line(lx, drawYMin, lx, drawYMax);


        }

        sr.end();

    }

}
