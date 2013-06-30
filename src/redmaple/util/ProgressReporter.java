package redmaple.util;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public interface ProgressReporter {
    public void progress(float percent);
    public void finished();
    public void failed(String message, Exception e);

}
