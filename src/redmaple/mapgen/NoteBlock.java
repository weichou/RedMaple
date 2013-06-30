package redmaple.mapgen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonWriter;
import redmaple.game.GameScreen;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 28.3.2013
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public class NoteBlock extends Block {

    float color;

    public NoteBlock() {
        int i = MathUtils.random(5);
        Color color = null;
        switch (i) {
            case 0: color = Color.RED; break;
            case 1: color = Color.BLUE; break;
            case 2: color = Color.ORANGE; break;
            case 3: color = Color.PINK; break;
            case 4: color = Color.GREEN; break;
            case 5: color = Color.YELLOW; break;
        }
        this.color = color.toFloatBits();
    }

    public NoteBlock color(float fbits) {
        this.color = fbits;
        return this;
    }

    @Override
    public Block src(float x, float y) {
        this.target(x+1, y+1);
        return super.src(x, y);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void notifyCollision(GameScreen screen) {

        if (screen.isDead())
            return;

        this.toggleState = false;

        screen.popNote(this);
    }

    @Override
    public void draw(GameScreen gameScreen, SpriteBatch batch, int blockSize) {
        float trx = this.x * blockSize;
        float trz = this.y * blockSize;

        Sprite note = gameScreen.musicNote;

        float scale = note.getScaleX();

        trx -= (note.getWidth() - note.getWidth() * scale) / 2;
        trz -= (note.getHeight() - note.getHeight() * scale) / 2;

        trz += MathUtils.sin((gameScreen.time + trx) * 0.01f /* trx = seed */) * 2.5f;

        note.setColor(Color.BLACK);
        note.setPosition(trx, trz);
        note.setPosition(trx, trz);
        note.draw(batch);

        //batch.draw(gameScreen.tileset, trx, trz, 0, 0, blockSize, blockSize, 1, 1, 0, (int) (1f / 8f * 256), uvY / 8, 32, 32, false, false);
    }

    @Override
    public void output(JsonWriter jw) throws IOException {
        jw.value(x);
        jw.value(y);
        jw.value(color);
    }

    @Override
    public void input(Array<Object> ao) {
        this.src((Float) ao.get(1), (Float) ao.get(2));
        this.color = (Float) ao.get(3);
    }
}
