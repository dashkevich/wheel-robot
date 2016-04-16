package robotics.wheeltest;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Бацька on 09.04.2016.
 */


public class SerialPort {

    private Context mContext;
    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private UsbSerialPort port;
    private SerialInputOutputManager.Listener mListener;
    public void addReadListener(SerialInputOutputManager.Listener listener){
        mListener = listener;
    }

    SerialPort(Context context){
        mContext = context;
    }

    public boolean connect(){

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.size() == 0) {
            return false;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);

        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            //manager.requestPermission(driver.getDevice(), mPermissionIntent);
            return false;
        }

        // Read some data! Most have just one port (port 0).
        port = driver.getPorts().get(0);

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public void write(byte[] data){
        mSerialIoManager.writeAsync(data);
    }

    public void closeConnection(){
        try {
            port.close();
        } catch (IOException e2) {
            // Ignore.
        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    public void startIoManager() {
        if (port != null) {
            mSerialIoManager = new SerialInputOutputManager(port, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }
}
