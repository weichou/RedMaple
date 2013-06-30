package redmaple.util;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 20:23
 * To change this template use File | Settings | File Templates.
 */
public class Mathx {
    /**
     *
     *  Returns a float that is the nearest one to number that is divisable by divisor and is smaller than number
     *
     * @param number
     * @param divisor
     * @return
     */
    public static float ceil(float number, float divisor) {
        return number - (number % divisor);
    }
}
