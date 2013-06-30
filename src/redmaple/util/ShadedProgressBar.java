package redmaple.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class ShadedProgressBar {
    private GameScreen screen;

    public ShadedProgressBar(GameScreen screen) {
        this.screen = screen;
    }

    public void draw(GameScreen.LoadState progressPhase, float progress) {
        final float progressBarHeight = 75;

        final ShapeRenderer shapeRenderer = screen.shapeRenderer;
        final float width = screen.camera.viewportWidth;
        final float height = screen.camera.viewportHeight;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.line(0, height - progressBarHeight, width, height - progressBarHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(0, height - progressBarHeight, width, height - progressBarHeight);

        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(0, height - progressBarHeight, width * progress, height - progressBarHeight);

        shapeRenderer.end();

        final SpriteBatch batch = screen.batch;
        final BitmapFont font = screen.font;

        batch.begin();
        batch.enableBlending();

        font.draw(batch, MathUtils.round(progress*100) + "%", width * progress - 45, height-progressBarHeight/2+7);

        String text;
        if (progressPhase == null)
            text = "Preparing..";
        else
            switch(progressPhase) {
                case SearchingCache:
                    text = "Searching for matching map in online cache";
                    break;
                case Analyzing:
                    text = "Analyzing sound data";
                    break;
                case GeneratingMap:
                    text = "Generating map";
                    break;
                case Finished:
                    text = "Finished!";
                    break;
                default:
                    text = "Unknown: " + progressPhase;
                    break;
            }

        font.draw(batch, text, 10, height - progressBarHeight - 15);

        batch.disableBlending();
        batch.end();


    }
}
