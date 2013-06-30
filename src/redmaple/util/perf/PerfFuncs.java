package redmaple.util.perf;

import com.badlogic.gdx.utils.Disposable;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 1.4.2013
 * Time: 17:36
 * To change this template use File | Settings | File Templates.
 */
public interface PerfFuncs extends Disposable {
    public int analyze(short[] samples, float[] flux, int fOffset, int fLength);
    public boolean isNative();
}
