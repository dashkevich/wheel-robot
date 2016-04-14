package com.hoho.android.usbserial.util;

/**
 * Created by Бацька on 10.04.2016.
 */

public interface SerialPortListener {
    /**
     * Called when new incoming data is available.
     */
    public void onNewData(byte[] data);

    /**
     * Called when {@link SerialInputOutputManager#run()} aborts due to an
     * error.
     */
    public void onRunError(Exception e);
}
