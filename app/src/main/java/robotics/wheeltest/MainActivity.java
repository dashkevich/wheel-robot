package robotics.wheeltest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    WiFiConnection connection;
    SerialPort serialPort;
    PacketParser packetParser;

    TextView textView;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            //connect();
                        }
                    }
                    else {
                        //Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        textView.setText("");

        //connection = new WiFiConnection();
        //connection.startServer();

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);


        serialPort = new SerialPort(this);
        packetParser = new PacketParser(serialPort);

        if(!serialPort.connect()){
            return;
        }

        serialPort.startIoManager();





        int i = 0;
        while(true){
            int n = packetParser.packetsAvailable();
            if(n > 0){
                PacketParser.PacketData p = packetParser.getPacket();
                ++i;
            }
        }




/*
        while(true) {
            byte buffer[] = {'+', 'I', 'P', 'D', 4, 1, 2, 3, 4};
            //serialPort.write(buffer);

            try {
                Thread.sleep(500);
            }catch (InterruptedException ex){

            }
        }
        */

    }


}
