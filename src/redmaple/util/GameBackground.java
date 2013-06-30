package redmaple.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class GameBackground {
    private final Mesh progressBarMesh;
    private final ShaderProgram progressBarShader;
    private GameScreen screen;

    public GameBackground(GameScreen screen) {
        this.screen = screen;

        this.progressBarMesh = Meshes.fullscreenMesh();
        progressBarShader = new ShaderProgram(Gdx.files.internal("shaders/background.vert"), Gdx.files.internal("shaders/background.frag"));
    }

    public void draw() {

        progressBarShader.begin();
        progressBarMesh.render(progressBarShader, GL10.GL_TRIANGLES);
        progressBarShader.end();



    }
}
