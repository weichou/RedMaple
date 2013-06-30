package redmaple.mapgen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import redmaple.audio.AudioProcessor;
import redmaple.game.GameScreen;
import redmaple.map.Map;
import redmaple.map.SpriteBlock;
import redmaple.util.Mathx;
import redmaple.util.ProgressReporter;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class MapGenerator {

    protected GameScreen screen;
    protected AudioProcessor audioProcessor;

    protected MapGenerator(GameScreen screen) {
        this.screen = screen;
        this.audioProcessor = screen.audioProcessor;
    }

    protected int getRuns() {return 1;}
    protected abstract void tick(int spectrumIndex, int run);
    protected void preProcess() {}
    protected void postProcess() {}

    public void generate(ProgressReporter pr) {
        preProcess();

        int spectrumCount = audioProcessor.getSpectrumCount();

        int runs = getRuns();
        for (int run = 0;run < runs; run++) {
            System.out.println("run #" + run);
            for (int sidx = 0; sidx < spectrumCount; sidx++) {
                tick(sidx, run);
                if (pr != null) {
                    float perRun = sidx / (float) spectrumCount / (float) runs;
                    pr.progress((run / (float)runs) + perRun);
                }
            }
        }

        if (pr != null) {
            pr.progress(1);
            pr.finished();
        }

        postProcess();

    }

    //protected float samplesPerBlock = MathUtils.nextPowerOfTwo(AudioProcessor.SPECTRUM_LENGTH / 3);
    protected float spectrumsPerBlock = AudioProcessor.SPECTRUM_LENGTH / 682f;
    {
        Gdx.app.log("RedMaple", "SPB: " + spectrumsPerBlock);
    }
    protected int blockSize = 32;

    protected float startY;

    public Map generatedMap() {
        Map map = new Map();
        map.blocks = this.blocks;
        map.spriteBlocks = this.spriteBlocks;
        map.spectrumsPerBlock = this.spectrumsPerBlock;
        map.blockSize = blockSize;
        map.startY = startY;
        return map;
    }

    public void dispose() {

    }

    protected float flux(int sidx) {
        return audioProcessor.computeFlux(sidx);
    }
    protected float thresholdMean(int sidx) {
        return audioProcessor.computeThresholdMean(sidx, 10);
    }
    protected float prunedFlux(int sidx) {
        return audioProcessor.computePrunnedFlux(sidx);
    }

    /**
     *  How many seconds we are in to the song at given spectrum index
     * @param sidx
     * @return seconds as float
     */
    protected float seconds(int sidx) {
        return audioProcessor.samplesPerSecond * sidx;
    }

    protected float sidxToMapX(int sidx) {
        return sidx / spectrumsPerBlock;
    }

    protected float toGridX(float x) {
        return Mathx.ceil(x, blockSize);
    }
    protected float toGridY(float y) {
        return Mathx.ceil(y, blockSize);
    }

    /**
     * @return if it's a peak, the strength of it. Otherwise 0
     */
    protected float peakStrength(int sidx) {
        return prunedFlux(sidx); // TODO could use some setting to change the multiplier
    }

    protected boolean isPeak(int sidx) {
        return peakStrength(sidx) > 0;
    }

    public int countPeaks(int sidx, int threshold) {
        int peaks = 0;
        for (int i = -threshold; i < threshold; i++) {
            if (isPeak(sidx+i))
                peaks++;
        }
        return peaks;
    }

    /**
     *  Use findEdgeCases() in preProcess to fill these
     */
    protected float maxFlux, maxPrunedFlux, maxThreshold, minFlux, minThreshold;

    /**
     *  Finds flux, prunedFlux and threshold edge cases (max values for all of them and min values for flux and threshold)
     */
    protected void findEdgeCases() {
        int spectrumCount = audioProcessor.getSpectrumCount();
        for (int sidx = 0; sidx < spectrumCount; sidx++) {
            float flux = flux(sidx), threshold = thresholdMean(sidx), prunedFlux = prunedFlux(sidx);

            maxFlux = Math.max(flux, maxFlux);
            maxPrunedFlux = Math.max(prunedFlux, maxPrunedFlux);
            maxThreshold = Math.max(threshold, maxThreshold);

            minFlux = Math.min(flux, minFlux);
            minThreshold = Math.min(threshold, minThreshold);
        }
    }

    protected float normalizePrunedFlux(float in) {
        return in / maxPrunedFlux;
    }

    protected float averagePPS;

    /**
     *  PPM (Peaks Per Minute) is essentially same as BPM, but because the peak detection algorithm isn't perfect (and cant be) we can't know the real BPM.
     *  PPS is Peaks Per Spectrum so always <1
     */
    protected void findPPMData() {
        int spectrumCount = audioProcessor.getSpectrumCount();
        int peaks = 0;
        for (int sidx = 0; sidx < spectrumCount; sidx++) {
            if (isPeak(sidx))
                peaks++;
        }
        averagePPS = (float) peaks / (float) spectrumCount;

    }

    protected int peaks(int sidx, int windowSize) {
        int start = Math.max(0, sidx - windowSize);
        int end = Math.min(audioProcessor.getSpectrumCount(), sidx + windowSize); // idk

        int peaks = 0;
        for (int i = start; i < end; i++) {
            if (isPeak(i))
                peaks++;
        }
        return peaks;
    }

    /**
     *  Hype is basically energy but on a lot bigger area. There should generally be at least one "high hype" and one "low hype" zone in song.
     *  Requires findPPMData()
     */
    public float hype(int sidx, int windowSize) {

        int start = Math.max(0, sidx - windowSize);
        int end = Math.min(audioProcessor.getSpectrumCount(), sidx + windowSize); // idk

        float mean = 0;
        for (int i = start; i < end; i++) {
            float se = getEnergy(i);

            mean += se; // To sampleIndex
        }

        mean /= (end - start);

        if (mean > 0.9f) { // if we got more than 0.9 from mere energies, we prob got some real stuff going on anyway so peaks arent needed

            // Peak stuff
            mean *= 0.5f; // What we have computed so far (energy mean) should only matter for 50% of hype, so dividing by 2

            int peaks = peaks(sidx, windowSize); // compute peaks in same area we are getting hype from

            mean += peaks / (averagePPS * windowSize * 2) * 0.4f;
            // averagePPS is Peaks Per Sample on average so whole multiplications expression gets average samples we should have within windowSize samples
            // then we simply divide computed peaks by what we should have in given window. So if there were more peaks than should be, result should be >1
            // and vice versa. 0.4f simply normalizes it to somewhat fit 0 to 1 range.

        }

        mean = MathUtils.clamp(mean, 0, 1); // Peak stuff might return more than 1

        return mean;
    }

    /**
     *
     *  Energy = threshold / maxThreshold. Requires findEdgeCases() in preProcess().
     *
     * @return
     */
    public float getEnergy(int sidx) {
        if (maxThreshold == 0) throw new UnsupportedOperationException("You need to call findEdgeCases() in preProcess() to enable getEnergy");
        return thresholdMean(sidx) / maxThreshold;
    }

    protected float groundYAt(int sidx) {
        for (Block b : blocks) {
            if (b.x < sidx && b.tx > sidx && b instanceof GroundBlock && b.y != 0)
                return b.y;
        }
        return -1;
    }

    public Array<Block> blocks = new Array<Block>();
    public Array<SpriteBlock> spriteBlocks = new Array<SpriteBlock>();

    protected void commit(Block block) {
        block.x /= spectrumsPerBlock;
        block.tx /= spectrumsPerBlock;

        this.blocks.add(block);
    }
    protected void commit(SpriteBlock sp) {
        sp.x /= spectrumsPerBlock;
        this.spriteBlocks.add(sp);
    }
}
