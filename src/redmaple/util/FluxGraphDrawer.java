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
public class FluxGraphDrawer {

    GameScreen screen;


    public FluxGraphDrawer(GameScreen screen) {
        this.screen = screen;
    }

    public void draw() {

        if (!screen.audioProcessor.hasFluxData())
            return;

        final ShapeRenderer sr = screen.shapeRenderer;

        float spectrumViewWidth = Gdx.graphics.getWidth();
        final float height = screen.camera.viewportHeight;
        final float graphScale = 0.5f;

        sr.begin(ShapeRenderer.ShapeType.Line);

        {
            Vector2 lastFlux = new Vector2(), lastPrunedFlux = new Vector2(), lastThreshold = new Vector2();

            final int curSpectrum = screen.audioProcessor.getLatencyAffectedSpectrumIndex();
            for (int i = -100; i < 0; i++) {
                int addSpectrum = MathUtils.clamp(curSpectrum + i, 0, screen.audioProcessor.getSpectrumCount());
                int addSample = addSpectrum * AudioProcessor.SPECTRUM_LENGTH;

                float flux = screen.audioProcessor.computeFlux(addSpectrum) * 0.1f;
                float prunedFlux = screen.audioProcessor.computePrunnedFlux(addSpectrum) * 0.1f;
                float threshold = screen.audioProcessor.computeThresholdMean(addSpectrum, 10) * 0.1f;

                float drawX = (i + 100) * 3;
                float drawYAdd = height - 150;

                sr.setColor(1, 0, 0, 1f);
                sr.line(lastFlux.x, lastFlux.y*graphScale + drawYAdd, drawX, flux*graphScale + drawYAdd);

                sr.setColor(0, 1, 0, 1f);
                sr.line(lastPrunedFlux.x, lastPrunedFlux.y*graphScale + drawYAdd, drawX, prunedFlux*graphScale + drawYAdd);

                sr.setColor(0, 0, 1, 1);
                sr.line(lastThreshold.x, lastThreshold.y*graphScale + drawYAdd, drawX, threshold*graphScale + drawYAdd);

                lastFlux.set(drawX, flux);
                lastPrunedFlux.set(drawX, prunedFlux);
                lastThreshold.set(drawX, threshold);

            }
        }

        sr.end();

    }

}
