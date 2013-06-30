package redmaple.android.RMAndroid;

import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.math.MathUtils;
import redmaple.audio.AudioProcessor;
import redmaple.util.perf.PerfFuncs;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 1.4.2013
 * Time: 17:43
 * To change this template use File | Settings | File Templates.
 */
public class AndroidPerfFuncs implements PerfFuncs {

    /*
    FFTAnalyzer fftAnalyzer;

    public AndroidPerfFuncs() {
        this.fftAnalyzer = new FFTAnalyzer();
    }

    @Override
    public int analyze(short[] samples, float[] flux, int fOffset, int fLength) {
        return fftAnalyzer.analyze(samples, flux, fOffset, fLength);
    }

    @Override
    public boolean isNative() {
        return true;
    }


    @Override
    public void dispose() {
        fftAnalyzer.dispose();
    }
    */

    KissFFT fft;

    public AndroidPerfFuncs() {
        this.fft = new KissFFT(AudioProcessor.LOADING_SPECTRUM_LENGTH);
    }

    float[] spectrum = new float[AudioProcessor.LOADING_SPECTRUM_LENGTH];
    float[] lastSpectrum = new float[AudioProcessor.SPECTRUM_LENGTH];

    protected void hamming(float[] samples)
    {
        for (int i = 0; i < samples.length; i++)
        {
            samples[i] *= (0.54f - 0.46f * MathUtils.cos(MathUtils.PI2 * i / (samples.length - 1)));
        }
    }

    @Override
    public int analyze(short[] samples, float[] flux, int fOffset, int fLength) {

        fft.spectrum(samples, spectrum);
        hamming(spectrum);

        final int sLength = samples.length/fLength;
        for (int f = 0; f < fLength; f++) {
            float nflux = 0;
            for( int i = 0; i < sLength; i++ ) {
                float lastSpectrumVal = lastSpectrum[i];
                float value = (spectrum[i + f*sLength] - lastSpectrumVal);
                nflux += value < 0? 0: value;
            }
            flux[fOffset+f] = nflux;

            System.arraycopy(spectrum, 0, lastSpectrum, 0, AudioProcessor.SPECTRUM_LENGTH);

        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
