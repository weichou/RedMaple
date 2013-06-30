package redmaple.map;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 26.3.2013
 * Time: 20:45
 * To change this template use File | Settings | File Templates.
 */
public class SpriteBlock {
    public String id;
    public Sprite sprite;

    public SpriteBlock(String id) {
        this.id = id;
    }

    public float x;
    public float y;

    public float scale = 1, alpha = 1;



    public SpriteBlock src(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public SpriteBlock scale(float v) {
        this.scale = v;
        return this;  //To change body of created methods use File | Settings | File Templates.
    }

    public SpriteBlock alpha(float v) {
        this.alpha = v;
        return this;
    }
}
