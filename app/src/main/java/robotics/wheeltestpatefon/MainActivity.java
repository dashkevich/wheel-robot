package robotics.wheeltestpatefon;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private WiFiConnection connection;
    private SerialPort serialPort;
    private PacketParser packetParser;
    private WiFiToSerialProxy wiFiToSerialProxy;


    //UI
    public TextView scrollView;
    public TextView textView;
    public TextView textView2;
    public TextView textView3;
    public Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        scrollView = (TextView) findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.append("Init...\n");
                int err = init();
                textView.append("Init status: " + err);
                button.setEnabled(false);
            }
        });

        //show IP adress
        WifiManager wifiMan = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        textView2.setText(ip);

    }

    int init(){


        //init serial port
        serialPort = new SerialPort(this);
        if(!serialPort.connect()){
            return -1;
        }

        packetParser = new PacketParser(serialPort);

        serialPort.startIoManager();

        connection = new WiFiConnection();

        wiFiToSerialProxy = new WiFiToSerialProxy(serialPort, packetParser, this);

        textView.append("Start server socket\n");

        connection.addNewClientEvent(new WiFiConnection.OnNewClientEvent() {
            @Override
            public void onConnect(Socket socket) {
                wiFiToSerialProxy.addSocket(socket);
            }
        });

        connection.startServer();
        wiFiToSerialProxy.start();

        return 0;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        wiFiToSerialProxy.interrupt();
        connection.stopServer();
        serialPort.stopIoManager();
        serialPort.closeConnection();

    }

}
