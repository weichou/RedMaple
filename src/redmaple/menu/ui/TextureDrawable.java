package redmaple.menu.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
public class TextureDrawable extends BaseDrawable {
    public Texture texture;

    public TextureDrawable(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
        batch.draw(texture, x, y, width, height);
    }
}
