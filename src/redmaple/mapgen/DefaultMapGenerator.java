package redmaple.mapgen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import redmaple.audio.AudioProcessor;
import redmaple.game.GameScreen;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
public class DefaultMapGenerator extends MapGenerator {
    public DefaultMapGenerator(GameScreen screen) {
        super(screen);
    }

    float[] yGraph;

    private static final int PRECISENESS = 50;

    @Override
    protected void preProcess() {
        findEdgeCases();
        findPPMData();

        // Ground

        Block block = new GroundBlock()
                .src(0, 0)
                .target(audioProcessor.getSpectrumCount(), 0)
                .height(0.7f);

        commit(block);

        yGraph = new float[audioProcessor.getSpectrumCount()/PRECISENESS + 1];
        for (int i = 0;i < yGraph.length; i++) {
            yGraph[i] = hype(i*PRECISENESS, PRECISENESS);
        }
    }

    int barTick;
    float lastYGraphed;

    private static final float REQ_JUMP_DIFF = 0.12f;
    private static final int HYPE_TO_Y = 12;
    private static final float GROUND_BLOCK_HEIGHT = 10f;

    private class LastHype {
        public float hype;
        public int sidx;
    }
    private LastHype lastHype = new LastHype();

    float lastY;

    @Override
    protected int getRuns() {
        return 1;
    }

    public void tickGround(int spectrumIndex) {
        int sidxDiff = spectrumIndex - lastHype.sidx;
        float curHype = hype(spectrumIndex, PRECISENESS);
        float hypeDiff = curHype - lastHype.hype;

        final float spectrumsPerSec = (audioProcessor.samplesPerSecond / AudioProcessor.SPECTRUM_LENGTH);
        final float minimumJumpDistance = spectrumsPerSec*0.7f;

        if ( spectrumIndex > 250 && Math.abs(hypeDiff) > REQ_JUMP_DIFF &&  // hypeDiff > required
                ((sidxDiff > minimumJumpDistance && peakStrength(spectrumIndex) > 200) || sidxDiff > 150)
                ) {

            int jumps = Math.max(1, MathUtils.ceil(Math.abs(hypeDiff) / REQ_JUMP_DIFF*2)) * 2 /* times 2 to negate first division */;
            float yOffPerJump = Math.signum(hypeDiff) * REQ_JUMP_DIFF * 12;

            float sidxDiffPerJump;

            do {
                jumps /= 2;
                sidxDiffPerJump = sidxDiff / (float)jumps;
            }
            while (sidxDiffPerJump < minimumJumpDistance);

            final float startX = spectrumIndex-sidxDiff;
            final float startLastY = lastY;

            for (int j = 0;j < jumps; j++) {
                float y = lastY;

                final float srcX = startX + sidxDiffPerJump*j;
                final float endX = startX + sidxDiffPerJump*(j+1);

                Block block = new GroundBlock()
                        .src(srcX, y)
                        .target(endX + 0.5f, y)
                        .height(GROUND_BLOCK_HEIGHT);
                commit(block);

                { // Add notes

                    int csrcX = MathUtils.ceil(srcX);
                    int cendX = MathUtils.ceil(endX);
                    int derp = 0;
                    int lastderp = 0;
                    for (int s = csrcX; s < cendX; s++) {

                        if (s < 250) // dont add anything <250
                            continue;

                        // shouldCompensate = -1 => note should be placed on level of last block
                        // shouldCompensate = 1 => note should be placed on level of next block
                        int shouldCompensate = 0;
                        if ((s-csrcX) < 3) {
                            shouldCompensate = -1;
                        }
                        else if ((cendX-s) < 3) {
                            shouldCompensate = 1;
                        }

                        if (shouldCompensate != 0) // TODO temp solution to just ignore the notes near edges
                            continue;

                        float placey = y;// - shouldCompensate*yOffPerJump; // Doesnt work.

                        float energyOrSomething = thresholdMean(s);
                        float ps = peakStrength(s);
                        if (energyOrSomething > 50 && ps > energyOrSomething/2) {

                            if (derp++ % 5 == 0 && Math.abs(s-csrcX) > 15 && Math.abs(s-cendX) > 30) {

                                Block nblock = new GroundBlock()
                                        .src(s - 3, placey + (GROUND_BLOCK_HEIGHT))
                                        .target(s + 3, placey + (GROUND_BLOCK_HEIGHT))
                                        .height(1.5f);
                                commit(nblock);

                                lastderp = s;

                            }
                            else if (Math.abs(lastderp-s) > 4) {

                                float strength = MathUtils.clamp(ps / 1000f, 0f, 1f);

                                Block nblock = new NoteBlock()
                                        .color(new Color(strength*1f, 0, 0, 1).toFloatBits())
                                        .src(s, placey + (GROUND_BLOCK_HEIGHT) + 0.8f);
                                commit(nblock);

                            }

                        }
                    }

                }

                lastY += yOffPerJump;
            }

            /*
            for (int i = 0;i < sidxDiff; i++) {
                int nsidx = spectrumIndex - i;
                int nthJump = MathUtils.ceil((sidxDiff-i) / sidxDiffPerJump);
                if (peakStrength(nsidx) > 100f) {

                    Block block = new NoteBlock()
                            .src(nsidx, (startLastY + yOffPerJump*nthJump) + (GROUND_BLOCK_HEIGHT));
                    commit(block);

                }
            }
            */

            lastHype.sidx = spectrumIndex;
            lastHype.hype = curHype;

            if (startY == 0) {
                startY = lastY;
            }

        }
    }


    @Override
    public void tick(int spectrumIndex, int run) {

        tickGround(spectrumIndex);
    }

}
