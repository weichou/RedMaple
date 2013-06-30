package redmaple.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.WindowedMean;
import redmaple.audio.AudioProcessor;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
public class SpectrumDrawer {

    GameScreen screen;

    float[] maxValues = new float[2048];
    float[] topValues = new float[2048];

    float spectrumViewWidth = 600;
    float spectrumViewHeight = 400;

    int NB_BARS = 31;
    float barWidth = (spectrumViewWidth / (float) NB_BARS);
    float FALLING_SPEED = (1.0f / 3.0f);

    float fluxCacheWidth = 0;

    public SpectrumDrawer(GameScreen screen) {
        this.screen = screen;
    }

    Sprite colors;


    public void init() {
        colors = screen.bigAtlas.createSprite("spectrum_colors");
    }

    public void draw() {

        if (!screen.audioProcessor.hasSpectrumData())
            return;

        final SpriteBatch batch = screen.batch;
        final ShapeRenderer sr = screen.shapeRenderer;

        batch.begin();
        batch.setProjectionMatrix(screen.camera.combined);

        int spectrumLength = AudioProcessor.SPECTRUM_LENGTH;
        int nth = screen.audioProcessor.getSampleIndex();

        float offX = 300;
        float offY = Gdx.graphics.getHeight() - 150;
        spectrumViewWidth = Gdx.graphics.getWidth() - offX;
        barWidth = (spectrumViewWidth / (float) NB_BARS);

        for (int i = 0; i < NB_BARS; i++) {
            int histoX = 0;
            if (i < NB_BARS / 2) {
                histoX = NB_BARS / 2 - i;
            } else {
                histoX = i - NB_BARS / 2;
            }

            int nb = (spectrumLength / NB_BARS) / 2;
            if (avg(nth + histoX, nb) > maxValues[histoX]) {
                maxValues[histoX] = avg(nth + histoX, nb);
            }

            if (avg(nth + histoX, nb) > topValues[histoX]) {
                topValues[histoX] = avg(nth + histoX, nb);
            }

// drawing spectrum (in blue)

            colors.setPosition(offX + i * barWidth, offY);
            colors.setSize(barWidth, scale(avg(nth + histoX, nb)));
            colors.setRegion(0, 0, 1, 5f/16f);

            colors.draw(batch);

            colors.setY(offY + scale(topValues[histoX]));
            colors.setSize(barWidth, 4);
            colors.setRegion(0, 5f/16f, 1, 5f/16f);

            colors.draw(batch);

            colors.setY(offY + scale(maxValues[histoX]));
            colors.setSize(barWidth, 2);
            colors.setRegion(0, 10f/16f, 1, 5f/16f);

            colors.draw(batch);

            /*
            batch.draw(colors, offX + i * barWidth, offY, barWidth,
                    scale(avg(nth + histoX, nb)), 0, 0, 16, 5, false, false);
// drawing max values (in yellow)
            batch.draw(colors, offX + i * barWidth, offY + scale(topValues[histoX]),
                    barWidth, 4, 0, 5, 16, 5, false, false);
// drawing top values (in red)
            batch.draw(colors, offX + i * barWidth, offY + scale(maxValues[histoX]),
                    barWidth, 2, 0, 10, 16, 5, false, false);
                    */

            topValues[histoX] -= FALLING_SPEED;
        }

        batch.end();

        /*float flux = screen.audioProcessor.computeFlux(screen.audioProcessor.getSampleIndex()) * 0.1f;
        sr.setColor(1, 0, 0, 1);
        sr.line(0, thresholdMean, spectrumViewWidth, thresholdMean);
        */
/*

        float[] fluxCache = screen.audioProcessor.getFluxCache();
        float[] thresholdCache = screen.audioProcessor.getThresholdCache();

        if (fluxCacheWidth == 0) {
            fluxCacheWidth = spectrumViewWidth / fluxCache.length;
        }

        Vector2 lastVec = new Vector2();
        for (int i = 0;i < fluxCache.length; i++) {

            if (i < fluxCache.length/2) // cached
                sr.setColor(1, 1, 0, 1);
            else                      // realtime
                sr.setColor(0, 1, 0, 1);

            int idx = (i + screen.audioProcessor.getRealtimePointer()) % fluxCache.length;

            float x = i * fluxCacheWidth;
            float y = fluxCache[idx] * 0.1f;

            sr.line(lastVec.x, lastVec.y, x, y);
            lastVec.set(x, y);

            y =  thresholdCache[idx];
        }

        lastVec.set(0, 0);
        for (int i = 0;i < fluxCache.length; i++) {

            sr.setColor(1, 0, 0, 1);

            int idx = (i + screen.audioProcessor.getRealtimePointer()) % fluxCache.length;

            float x = i * fluxCacheWidth;
            float y = thresholdCache[idx] * 0.1f;

            sr.line(lastVec.x, lastVec.y, x, y);
            lastVec.set(x, y);
        }

*/

    }

    private float scale(float x) {
        return x / 256 * spectrumViewHeight;
    }

    private float avg(int pos, int nb) {

        int sum = 0;
        for (int i = 0; i < nb; i++) {
            sum += screen.audioProcessor.getSpectrumAt(pos + i);
        }

        return (float) (sum / nb);
    }

    public void dispose() {
        colors.getTexture().dispose();
    }
}
