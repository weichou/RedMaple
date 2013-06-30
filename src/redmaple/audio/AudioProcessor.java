package redmaple.audio;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.io.Decoder;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.audio.io.VorbisDecoder;
import com.badlogic.gdx.audio.io.WavDecoder;
import com.badlogic.gdx.audio.transform.SoundTouch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import redmaple.GameSettings;
import redmaple.RedMaple;
import redmaple.util.ProgressReporter;
import redmaple.util.perf.PerfFuncs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class AudioProcessor {
    private FileHandle originalHandle;
    private FileHandle handle;

    private Decoder decoder;

    private AudioDevice device;

    /*

     README!!

        spectrumIndex = sampleIndex / SPECTRUM_SIZE
        so sampleIndex = nth sample

     */


    public AudioProcessor(FileHandle handle) {
        this.originalHandle = handle;
        this.handle = handle;
    }

    public FileHandle getOriginalHandle() {
        return originalHandle;
    }

    private Decoder getDecoder() {
        final String lowerName = handle.name().toLowerCase();
        if (lowerName.endsWith(".ogg"))
            return new VorbisDecoder(handle);
        else if (lowerName.endsWith(".mp3"))
            return new Mpg123Decoder(handle);
        else if (lowerName.endsWith(".wav"))
            return new WavDecoder(handle);
        else
            throw new UnsupportedOperationException("Loading invalid type redmaple.android.RMAndroid.audio handle: " + handle); // TODO give better WTF report
    }
    private void findDecoder() {
        decoder = getDecoder();
    }

    private float loadingState = 0;
    public float getLoadingState() {
        return loadingState;
    }

    public float samplesPerSecond;

    public void loadInit(ProgressReporter progressReporter) {
        try {
            findDecoder();
        }
        catch (UnsupportedOperationException uoe) {
            loadingState = -1;
            Gdx.app.error("RedMaple", "UOE while trying to find decoder for " + handle + ": " + uoe);
            if (progressReporter != null)
                progressReporter.failed("Invalid/corrupted file format. MP3, WAV and OGG supported", uoe);
            return;
        }

        // TODO could implement threading here
        device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1);
        Gdx.app.log("RedMaple", "Device latency (from AudioDevice): " + device.getLatency());
        this.audioLatency = device.getLatency();

        this.samplesPerSecond = decoder.getRate() * decoder.getChannels();

    }

    public void loadAnalyzer(ProgressReporter progressReporter) {

        if (handle.type() == Files.FileType.Internal) {
            handle = Gdx.files.external("tmp/test." + originalHandle.extension());
            originalHandle.copyTo(handle);
        }

        loadingState = 0;

        int samplesAmount = MathUtils.ceil((decoder.getLength()+1) * decoder.getRate() * decoder.getChannels());

        //fluxCache = new float[MathUtils.ceil(samplesAmount / (float)SPECTRUM_LENGTH)];

        if (GameSettings.Debug) {
            spectrumCache = new float[samplesAmount];
            int bytesPerElement = 4;

            Gdx.app.log("RedMaple", "SpectrumCache size: " + spectrumCache.length * (bytesPerElement));
        }

        fluxCache = new float[samplesAmount/SPECTRUM_LENGTH];

        Gdx.app.log("RedMaple", samplesAmount + " samples in " + getOriginalHandle().name());
        Gdx.app.log("RedMaple", "So we have " + fluxCache.length + " FluxCaches");

        int readSamples;
        int treadSamples = 0;

        int nthSpectrum = 0;

        short[] tempSamples = new short[LOADING_SPECTRUM_LENGTH];

        long loadingStarted = System.currentTimeMillis();

        final PerfFuncs pf = RedMaple.perfFuncs;

        final int fOffset = LOADING_SPECTRUM_LENGTH/SPECTRUM_LENGTH;
        int fLength = 0;

        while ((readSamples = decoder.readSamples(tempSamples, 0, LOADING_SPECTRUM_LENGTH)) > 0 && !Thread.currentThread().isInterrupted()) {

            if (readSamples < LOADING_SPECTRUM_LENGTH) {
                // fills tempSamples with zeroes to prevent "ghost" spectrums
                Arrays.fill(tempSamples, (short)0);
            }

            pf.analyze(tempSamples, fluxCache, fLength, fOffset);
            fLength += fOffset;

            /*
            if (GameSettings.Debug) {
                /*for (int x = treadSamples; x < treadSamples+readSamples; x++) {
                    spectrumCache[x] = PrimitiveCompressor.compressNormalizedFloatToShort(spectrum[x-treadSamples] / SPECTRUM_COMPRESSION);
                }*
                System.arraycopy(spectrum, 0, spectrumCache, treadSamples, readSamples);
            }
            TODO
            */

            /*
            {

                float flux = 0;
                for( int i = 0; i < SPECTRUM_LENGTH; i++ ) {
                    float lastSpectrumVal = lastSpectrum[i];
                    float value = (spectrum[i] - lastSpectrumVal);
                    flux += value < 0? 0: value;
                }
                fluxCache[nthSpectrum] = flux;

                System.arraycopy(spectrum, 0, lastSpectrum, 0, SPECTRUM_LENGTH);
            }
            */

            //System.arraycopy(spectrum, 0, spectrumCache, treadSamples, readSamples);

            float progress = (float)treadSamples / (float) samplesAmount;

            if (progressReporter != null) {
                progressReporter.progress(progress);
            }
            loadingState = progress;

            treadSamples += readSamples;
            nthSpectrum++;

            Thread.yield();

            //Gdx.app.log("RedMaple", "Progress! (" + readSamples + ") " + treadSamples + " / " + samplesAmount);
        }

        if (progressReporter != null) {
            progressReporter.progress(1);
            progressReporter.finished();
        }
        loadingState = 1;

        Gdx.app.log("RedMaple", "Loading " + handle.name() + " took " + (System.currentTimeMillis()-loadingStarted) + " ms");

    }

    public final static int SPECTRUM_LENGTH = 2048; // 2048
    public final static int LOADING_SPECTRUM_LENGTH = SPECTRUM_LENGTH; // 32768
    public final static float OKAY_THRESHOLD_MULTIPLIER = 1.5f;

    float[] spectrumCache;
    float[] fluxCache;


    public float computeFlux(int spectrumIndex) {
        if (spectrumIndex < 0 || spectrumIndex >= fluxCache.length) return 0;
        return fluxCache[spectrumIndex];
    }

    public float computePrunnedFlux(int spectrumIndex) {
        return computePrunnedFlux(spectrumIndex, OKAY_THRESHOLD_MULTIPLIER);
    }
    public float computePrunnedFlux(int spectrumIndex, float multiplier) {
        float flux = computeFlux(spectrumIndex);
        float threshold = computeThresholdMean(spectrumIndex, 10) * multiplier;
        if (threshold <= flux)
            return flux - threshold;
        return 0;
    }
    public float computePrunnedFlux(float flux, float threshold) {
        if (threshold <= flux)
            return flux - threshold;
        return 0;
    }

    public float computeThresholdMean(int spectrumIndex, int windowSize) {

        int start = Math.max(0, spectrumIndex - windowSize);
        int end = Math.min(fluxCache.length, spectrumIndex + windowSize); // idk

        float mean = 0;
        for (int i = start; i < end; i++)
            mean += computeFlux(i); // To sampleIndex

        mean /= (end - start);

        return mean;

    }

    public int getSampleIndex() {
        return playingSample;
    }
    public int getSpectrumIndex() {
        return getSampleIndex() / SPECTRUM_LENGTH;
    }

    public int getLatencyAffectedSampleIndex() {
        return Math.max(playingSample - audioLatency, 0);
    }

    public int getLatencyAffectedSpectrumIndex() {
        return getLatencyAffectedSampleIndex() / SPECTRUM_LENGTH;
    }

    /**
     *
     *  ONLY USABLE IF DEBUGGING!
     *
     * @param sampleIndex
     * @return
     */
    public float getSpectrumAt(int sampleIndex) {
        return spectrumCache[sampleIndex];
    }

    public float getFluxAtSecond(float second) {
        int sidx = (int) (samplesPerSecond * second);
        sidx -= sidx % SPECTRUM_LENGTH; // Rounds the sample index to nearest index that can be divided by SPECTRUM_LENGTH. There's prob an easier way
        return computeFlux(sidx / SPECTRUM_LENGTH);
    }

    /**
     *  DEBUGGING ONLY!!
     */
    public void generateThresholdGraph() {

        float highestFlux = 0;
        for (float f : fluxCache)
            highestFlux = Math.max(f, highestFlux);

        BufferedImage img = new BufferedImage(fluxCache.length, (int) (highestFlux+10) / 10, BufferedImage.TYPE_3BYTE_BGR);
        int height = img.getHeight();

        Graphics g = img.getGraphics();

        Point lastThreshold = new Point(), lastFlux = new Point();
        for (int s = 0;s < fluxCache.length; s++) {
            {
                int ny = height - (int)computeThresholdMean(s, 10)/10;
                g.setColor(Color.red);
                g.drawLine(lastThreshold.x, lastThreshold.y, s, ny);
                lastThreshold.x = s;
                lastThreshold.y = ny;
            }
            {
                int ny = height - (int) computeFlux(s)/10;
                g.setColor(Color.green);
                g.drawLine(lastFlux.x, lastFlux.y, s, ny);
                lastFlux.x = s;
                lastFlux.y = ny;
            }
        }

        try {
            ImageIO.write(img, "png", new File("thresholdgraph.png"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }




    protected void hamming(float[] samples)
    {
        for (int i = 0; i < samples.length; i++)
        {
            samples[i] *= (0.54f - 0.46f * MathUtils.cos(MathUtils.PI2 * i / (samples.length - 1)));
        }
    }
    protected void hamming(short[] samples) {

        /*
        final short hammingSub = (short) (0.54f * Short.MAX_VALUE);
        final short hammingMul = (short) (0.46f * Short.MAX_VALUE);

        for (int i = 0; i < samples.length; i++) {
            samples[i] *= (hammingSub - hammingMul * MathUtils.cos(MathUtils.PI2 * i / (samples.length - 1)));
        }
        */

    }

    int playingSample = 0;

    int audioLatency;

    public void rewind() {

        decoder.setPosition(0);
        if (decoder.getPosition() != 0) {
            // Failed to set position. This happens at least with mp3 decoder. Sucks but we'll have to deal with it
            decoder.dispose();
            decoder = getDecoder();
            Gdx.app.log("RedMaple", "Recreated decoder");
        }
        this.treadSamples = 0;
    }

    private volatile PlayState playState = PlayState.Playing;
    private final Object playStateLock = new Object();

    public void setPlayState(PlayState playState_) {
        this.playState = playState_;
        synchronized (this.playStateLock) {
            //System.out.println("Playstate set to " + playState + "; notifying");
            this.playStateLock.notify();
        }
    }

    private volatile int treadSamples;
    private volatile int toSkip = 0;
    public SoundTouch soundTouch = new SoundTouch();

    public void play() {
        int readSamples = 0;

        rewind();

        short[] samples = new short[2048];
        soundTouch.setChannels(decoder.getChannels());
        soundTouch.setSampleRate(decoder.getRate());

        while (!Thread.currentThread().isInterrupted()) {
            if (this.playState == PlayState.Paused) {
                synchronized (this.playStateLock) { // This is terrible. Meh
                    try {
                        this.playStateLock.wait();
                    } catch (InterruptedException e) {return;}
                }
            }

            int skipped = 0;
            if (toSkip != 0) {
                decoder.skipSamples((treadSamples + toSkip) / decoder.getChannels());
                skipped = toSkip;
                toSkip = 0;
            }

            if ((readSamples = decoder.readSamples(samples, 0, samples.length)) > 0) {

                //float pst = MathUtils.sin(TimeUtils.millis());
                //System.out.println(pst);
                //soundTouch.setPitchSemiTones(pst);
                soundTouch.putSamples(samples, 0, readSamples / decoder.getChannels());

                playingSample = (treadSamples += readSamples + skipped);

                readSamples = soundTouch.receiveSamples(samples, 0, samples.length / decoder.getChannels());

                device.writeSamples(samples, 0, readSamples * decoder.getChannels());

                Thread.yield();
            }
            else
                break;
        }
    }

    public void dispose() {
        if (handle != originalHandle && handle != null) {
            handle.delete();
        }
        if (device != null)
            device.dispose();
        if (decoder != null)
            decoder.dispose();
    }

    public int getSpectrumCount() {
        return fluxCache.length;
    }

    public PlayState getPlayState() {
        return playState;
    }

    public float getLength() {
        return decoder.getLength();
    }

    public void skipSamples(int s) {
        toSkip = s;
    }

    private float volume;

    public void setVolume(float volume) {
        this.volume = volume;
        this.device.setVolume(volume);
    }

    public boolean hasSpectrumData() {
        return spectrumCache != null;
    }

    public boolean hasFluxData() {
        return fluxCache != null;
    }

    public float getVolume() {
        return volume;
    }

    public enum PlayState {
        Playing, Paused
    }
}
