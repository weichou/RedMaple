package redmaple.mapgen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonWriter;
import redmaple.game.GameScreen;
import redmaple.util.Temps;

import java.io.IOException;

public abstract class Block {

    public float x, y; // source
    public float tx, ty; // target

    public float height = 1;

    public int uvX, uvY;
    public boolean toggleState = true; // enable/disabled

    public Block src(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Block target(float x, float y) {
        this.tx = x;
        this.ty = y;
        return this;
    }

    public Block height(float x) {
        this.height = x;
        return this;
    }

    public float collides(Rectangle bb, int blockSize, float offX, float offY) {
        Rectangle tmpRect = Temps.tmpRect;

        float fx = x * blockSize;
        float fy = y * blockSize;
        float fwidth = blockSize * tx - fx;
        float fheight = blockSize * height;
        tmpRect.set(fx - offX, fy - offY, fwidth, fheight);

        return bb.overlaps(tmpRect) ? 0 : -1;
    }

    public void notifyCollision(GameScreen screen) {}

    public abstract void output(JsonWriter jw) throws IOException;

    public abstract void input(Array<Object> ao);

    public void draw(GameScreen gameScreen, SpriteBatch batch, int blockSize) {}
}
