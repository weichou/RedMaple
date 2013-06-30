package redmaple.map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import redmaple.mapgen.Block;
import redmaple.mapgen.GroundBlock;
import redmaple.mapgen.NoteBlock;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class Map {

    public static final int CURRENT_VERSION = 2;

    public int blockSize;
    public float spectrumsPerBlock;
    public Array<Block> blocks;

    public Array<BackgroundMarker> bgMarkers = new Array<BackgroundMarker>();

    public String atlasPack = "africa.pack";
    public TextureAtlas textureAtlas;
    public Array<SpriteBlock> spriteBlocks;
    public float startY;

    public int version;

    private Block createBlock(int idx) {
        switch (idx) {
            case 0: return new GroundBlock();
            case 1: return new NoteBlock();
            case 2: throw new NotImplementedException();
        }
        throw new UnsupportedOperationException();
    }
    private int getIdx(Block b) {
        if (b instanceof GroundBlock) return 0;
        if (b instanceof NoteBlock) return 1;
        // DURR if (b instanceof SpriteBlock) return 2;
        return -1;
    }

    /*
    JSON EXPLANATION:

    Blocks:
        a = block id

     */

    public String createJson() {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        jw.setOutputType(JsonWriter.OutputType.minimal); // TODO change to "minimal" in production

        try {

            jw.object()
                    .set("bs", blockSize)
                    .set("createTime", System.currentTimeMillis())
                    .set("spb", spectrumsPerBlock)
                    .set("vers", CURRENT_VERSION)
                    .set("start", startY)
                    .set("ap", atlasPack);

            {
                jw.array("blcks");

                for (Block b : blocks) {
                    jw.array()
                            .value(getIdx(b));
                    b.output(jw);
                    jw.pop();
                }

                jw.pop();
            }
            {
                jw.array("bgms");

                for (BackgroundMarker bm : bgMarkers) {
                    jw.array();
                    bm.output(jw);
                    jw.pop();
                }

                jw.pop();
            }

            jw.pop();

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return sw.toString();
    }
    public void loadFromJson(String json) {
        JsonReader jr = new JsonReader();
        OrderedMap<String, Object> rootMap = (OrderedMap<String, Object>) jr.parse(json);

        this.version = (int) (float) (Float) rootMap.get("vers");
        this.blockSize = (int) (float) (Float) rootMap.get("bs");
        this.spectrumsPerBlock = (Float) rootMap.get("spb");
        this.atlasPack = (String) rootMap.get("ap");
        this.startY = (Float) rootMap.get("start");

        {

            Array<Object> jblocks = (Array<Object>) rootMap.get("blcks");
            this.blocks = new Array<Block>(jblocks.size);

            for (Object o : jblocks) {
                Array<Object> ao = (Array<Object>) o;

                Block block = createBlock((int) (float) (Float) ao.get(0));
                block.input(ao);

                this.blocks.add(block);
            }

        }

        if (version >= 2) {

            Array<Object> bgms = (Array<Object>) rootMap.get("bgms");
            this.bgMarkers = new Array<BackgroundMarker>(bgms.size);

            for (Object o : bgms) {
                Array<Object> ao = (Array<Object>) o;

                BackgroundMarker bm = new BackgroundMarker();
                bm.input(ao);

                this.bgMarkers.add(bm);
            }

        }

    }

    /**
     *  Called after loadFromJson() to see if we need to regenerate the map.
     * @return
     */
    public boolean isVersionSupported() {
        return true;
    }

    public static void main(String[] args) {
        Map map = new Map();
        map.blocks = new Array<Block>();

        for (int i = 0;i < 100; i++)
            map.blocks.add(new GroundBlock());

        String json = map.createJson();
        System.out.println(json);
        System.out.println("ByteCount: " + json.length()*2);
        map.loadFromJson(json);
    }
}
