package redmaple.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import redmaple.RedMaple;
import redmaple.menu.ui.TextureDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public class MenuScreen implements Screen {

    public RedMaple redMaple;

    public Stage stage;

    public MenuUI menuUi;

    public SpriteBatch batch;

    public Texture background;

    public MenuScreen(RedMaple redMaple) {
        RedMaple.songfinder.getStoredMusic();

        this.redMaple = redMaple;

        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.background = new Texture(Gdx.files.internal("gfx/menubg.png"));

        this.menuUi = new MenuUI(this);
        this.menuUi.create();

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        //Table.drawDebug(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.setViewport(width, height, true);
    }
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCatchBackKey(false);
    }

    @Override
    public void pause() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

}
