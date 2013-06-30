package redmaple.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class Meshes {
    public static Mesh fullscreenMesh() {
        Mesh mesh = new Mesh(true, 6, 0, VertexAttribute.Position(), VertexAttribute.TexCoords(0));

        mesh.setVertices(new float[] {
            -1, -1, 0,   0, 1,
            1, -1, 0,    1, 1,
            1, 1, 0,     1, 0,

            -1, -1, 0,   0, 1,
            1, 1, 0,     1, 0,
            -1, 1, 0,    0, 0

        });

        return mesh;
    }
}
