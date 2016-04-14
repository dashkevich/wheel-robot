package robotics.wheeltest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.util.SerialPortListener;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Бацька on 09.04.2016.
 */


public class SerialPort {

    private Context mContext;

    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private UsbSerialPort port;


    private SerialPortListener mListener;


    public void addReadListener(SerialPortListener listener){
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
