package redmaple.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import redmaple.game.GameScreen;
import redmaple.map.Map;
import redmaple.mapgen.Block;
import redmaple.mapgen.GroundBlock;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class Player {
    GameScreen screen;

    public Player(GameScreen screen) {
        this.screen = screen;
    }


    SkeletonData skeletonData;
    Skeleton skeleton;

    Animation runAnimation, jumpAnimation, slideAnimation;

    AnimationStateData animationMixing;
    AnimationState animationState;

    public Rectangle bb;
    private Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE), max = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
    private void updateBb(Bone root) {
        min.set(Float.MAX_VALUE, Float.MAX_VALUE);
        max.set(Float.MIN_VALUE, Float.MIN_VALUE);
        /*
        for (Bone bone : skeleton.getBones()) {
            if (bone == root)
                continue;
            min.x = Math.min(bone.getX(), min.x);
            min.y = Math.min(bone.getY(), min.y);

            max.x = Math.max(bone.getX(), max.x);
            max.y = Math.max(bone.getY(), max.y);
        }
        */
        Array<Bone> bones = skeleton.getBones();
        for (int i = 0, n = bones.size; i < n; i++) {
            Bone bone = bones.get(i);
            if (bone.getParent() == null) continue;

            float sx = bone.getWorldX();
            float sy = bone.getWorldY();
            float ex = bone.getData().getLength() * bone.getM00() + sx;
            float ey = bone.getData().getLength() * bone.getM10() + sy;

            min.x = Math.min(ex, min.x);
            min.y = Math.min(ey, min.y);

            max.x = Math.max(ex, max.x);
            max.y = Math.max(ey, max.y);

        }

        bb.set(min.x, min.y, max.x-min.x, max.y-min.y);
    }

    public void create() {

        final String name = "spineboy";

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skeleton/"  + name + ".atlas"));

        if (true) {
            SkeletonJson json = new SkeletonJson(atlas);
// json.setScale(2);
            skeletonData = json.readSkeletonData(Gdx.files.internal("skeleton/"  + name + "-skeleton.json"));
            runAnimation = json.readAnimation(Gdx.files.internal("skeleton/"  + name + "-run.json"), skeletonData);
            jumpAnimation = json.readAnimation(Gdx.files.internal("skeleton/"  + name + "-jump.json"), skeletonData);
            slideAnimation = json.readAnimation(Gdx.files.internal("skeleton/" + name + "-slide.json"), skeletonData);
        } else {
            SkeletonBinary binary = new SkeletonBinary(atlas);
// binary.setScale(2);
            skeletonData = binary.readSkeletonData(Gdx.files.internal("skeleton/"  + name + ".skel"));
            runAnimation = binary.readAnimation(Gdx.files.internal("skeleton/"  + name + "-walk.anim"), skeletonData);
            jumpAnimation = binary.readAnimation(Gdx.files.internal("skeleton/"  + name + "-jump.anim"), skeletonData);
        }

        animationMixing = new AnimationStateData();
        animationMixing.setMixing(slideAnimation, runAnimation, 0.4f);
        animationMixing.setMixing(runAnimation, jumpAnimation, 0.4f);
        animationMixing.setMixing(jumpAnimation, runAnimation, 0.4f);
        animationMixing.setMixing(slideAnimation, jumpAnimation, 0.4f);
        animationMixing.setMixing(jumpAnimation, slideAnimation, 0.2f);
        animationMixing.setMixing(runAnimation, slideAnimation, 0.2f);

        animationState = new AnimationState(animationMixing);
        animationState.setAnimation(runAnimation, true);

        skeleton = new Skeleton(skeletonData);
        skeleton.setToBindPose();

        final Bone root = skeleton.getRootBone();

        skeleton.updateWorldTransform();

        bb = new Rectangle();
        updateBb(root);

    }

    float time;

    ShapeRenderer sr;

    private float deathTimeLeft() {
        if (screen.deathTime == 0)
            return 0;
        return screen.deathTime - screen.relativeTime;
    }

    private boolean visStatus = false;
    private float nextUpdate, maxTime;

    public void render() {

        boolean vis = true;

        float tl = deathTimeLeft();
        if (tl > 0) {
            if (nextUpdate == 0)
                maxTime = tl;

            if (nextUpdate == 0 || nextUpdate < screen.relativeTime) {
                visStatus = !visStatus;
                float nu = MathUtils.clamp((maxTime - tl) * 0.1f, 0.1f, 1.0f);
                //System.out.println("TL: " + tl + " NU: " + nu);
                nextUpdate = screen.relativeTime + nu;
            }
            vis = visStatus;
        }
        else
            nextUpdate = 0;

        if (!vis)
            return;

        final SpriteBatch batch = screen.batch;

        batch.begin();
        batch.enableBlending();
        skeleton.draw(batch);
        batch.disableBlending();
        batch.end();

        //
        //System.out.println(bb);

        /*if (sr == null)
            sr = new ShapeRenderer();
        sr.setProjectionMatrix(screen.camera.combined);

        //skeleton.drawDebug(sr);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.rect(bb.x, bb.y, bb.width, bb.height);
        sr.end();
        */

    }

    public float x, y;

    public void setPosition(float x, int y) {
        this.x = x;
        this.y = y;
    }

    private float verticalVelocity = 0;

    private Array<Block> blockz = new Array<Block>(10);

    public Array<Block> touchesBlock(float offX, float offY) {

        blockz.size = 0;

        //final float mywx = bb.x + bb.width;
        //final float myx = bb.x;

        final Map map = screen.map;
        if (map == null)
            return blockz;

        final int blockSize = map.blockSize;

        for (Block b : map.blocks) {

            if (!b.toggleState)
                continue;

            /*if (b.x*blockSize > mywx)
                continue;

            if (b.tx*blockSize < myx)
                continue;
*/

            //System.out.println("testing " + b.x);

            if (b.collides(bb, blockSize, offX, offY) != -1) {
                blockz.add(b);
            }

        }
        return blockz;
    }
    public Array<Block> touchesBlock() {
        return touchesBlock(0, 0);
    }

    float endSlide;

    public void update(float delta) {

        // Physics stuff

        float wouldSink = verticalVelocity * delta;
        Array<Block> gravityBlock = touchesBlock(0, wouldSink);

        boolean applyGravity = true;

        for (Block b : gravityBlock) {
            b.notifyCollision(screen);
            if (b instanceof GroundBlock)
                applyGravity = false;
        }

        float touchFix = 0;

        if (applyGravity) {
            verticalVelocity -= 1200f * delta;
        }
        else {
            verticalVelocity = Math.max(0, verticalVelocity);
            //System.out.println(verticalVelocity);
            if (verticalVelocity == 0 && touchesBlock(0, -3) != null) {
                verticalVelocity += (touchFix = 50f); // Our boots are in ground so give a slight bump upwards
            }
        }
        if (applyGravity || verticalVelocity > 0)
            y += verticalVelocity * delta;

        verticalVelocity -= touchFix;

        // Animation stuff

        if (touchesGround(0, 0) && ((animationState.getAnimation() == jumpAnimation && verticalVelocity < 50) || animationState.getAnimation() == slideAnimation && endSlide != 0 && endSlide < time)) {
            animationState.setAnimation(runAnimation, true);
            endSlide = 0;
        }

        Bone root = skeleton.getRootBone();

        root.setX(x);
        root.setY(y);

        float mdelta = delta; // 1f = speed mul

        if (animationState.getAnimation() == runAnimation)
            mdelta *= 2;

        time += mdelta;

        animationState.update(mdelta);
        animationState.apply(skeleton);

        float scale = 0.3f;
        root.setScaleX(scale);
        root.setScaleY(scale);

        skeleton.updateWorldTransform();
        skeleton.update(delta);

        updateBb(root);

    }

    public boolean touchesGround(float offX, float offY) {
        Array<Block> blockz = touchesBlock(offX, offY);
        for (Block b : blockz)
            if (b instanceof GroundBlock)
                return true;
        return false;
    }

    public boolean touchesHorizontally() {
        boolean initial = touchesGround(0, 3);
        boolean second = touchesGround(-bb.width, 3);
        return initial && !second;
    }

    public void jump() {

        if (animationState.getAnimation() == slideAnimation)
            return;

        boolean b = touchesGround(0, 0) || touchesGround(0, -6) || touchesGround(0, -23); // Hacky Approaches Inc.
        if (b && verticalVelocity < 200) {
            verticalVelocity = 460;
            //animationState.setAnimation(jumpAnimation, false);
        }
    }

    public void slide(float time) {
        animationState.setAnimation(slideAnimation, true);
        if (time == 0)
            this.endSlide = 0;
        else
            this.endSlide = this.time + time;
    }

    public void endSlide() {
        animationState.setAnimation(runAnimation, true);
    }
}
