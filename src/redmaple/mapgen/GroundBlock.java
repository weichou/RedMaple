package redmaple.mapgen;

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
 * Date: 27.3.2013
 * Time: 21:35
 * To change this template use File | Settings | File Templates.
 */
public class GroundBlock extends Block {

    @Override
    public void draw(GameScreen gameScreen, SpriteBatch batch, int blockSize) {

        float trx = this.x * blockSize;
        float trz = this.y * blockSize;
        float trwidth = this.tx * blockSize - trx; // Transformed Width
        float trty = this.height * blockSize; // Transformed Target Y

        float rotation = 0;
        /*if (y != ty) {
            rotation = MathUtils.atan2(ty - y, tx - x) * MathUtils.radiansToDegrees;
        }*/

        //batch.draw(tileset, x, b.y * map.blockSize, width-x, map.blockSize * b.height, uvX / 8, uvY / 8, 32, 32, false, false);

        Sprite sprite = gameScreen.blackSprite;
        sprite.setPosition(trx, trz);
        sprite.setSize(trwidth, trty);

        sprite.draw(batch);

    }

    @Override
    public void output(JsonWriter jw) throws IOException {
        jw.value(x);
        jw.value(y);
        jw.value(tx);
        jw.value(ty);
        jw.value(height);
    }

    @Override
    public void input(Array<Object> ao) {
        this.x = (Float) ao.get(1);
        this.y = (Float) ao.get(2);
        this.tx = (Float) ao.get(3);
        this.ty = (Float) ao.get(4);
        this.height = (Float) ao.get(5);
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
