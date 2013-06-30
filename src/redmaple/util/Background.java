package redmaple.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 24.3.2013
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public class Background {

    GameScreen screen;

    public Background(GameScreen screen) {
        this.screen = screen;
    }

    ShaderProgram shader;
    Mesh fsMesh;

    public void create() {
        reloadShader();
        this.fsMesh = Meshes.fullscreenMesh();
    }

    public void draw() {

        this.shader.begin();
        //this.shader.setUniformf("time", screen.relativeTime);
        //this.shader.setUniformf("resolution", screen.camera.viewportWidth, screen.camera.viewportHeight);
        this.fsMesh.render(this.shader, GL10.GL_TRIANGLES);
        this.shader.end();

    }

    public void reloadShader() {
        this.shader = new ShaderProgram(Gdx.files.internal("shaders/background.vert"), Gdx.files.internal("shaders/background.frag"));
        if (!this.shader.isCompiled())
            throw new UnsupportedOperationException("Shadercompile failed: " + shader.getLog());
        System.out.println("Background shader loaded");
    }
}
