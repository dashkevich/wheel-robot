package robotics.wheeltest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private WiFiConnection connection;
    private SerialPort serialPort;
    private PacketParser packetParser;
    private WiFiToSerialProxy wiFiToSerialProxy;
    public TextView scrollView;

    //UI
    private TextView textView;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);
        scrollView = (TextView) findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.append("Init...\n");
                int err = init();
                textView.append("Init status: " + err);
            }
        });
    }

    int init(){

        //init serial port
        serialPort = new SerialPort(this);
        if(!serialPort.connect()){
            return -1;
        }
        serialPort.startIoManager();


        packetParser = new PacketParser(serialPort);

        connection = new WiFiConnection();

        wiFiToSerialProxy = new WiFiToSerialProxy(serialPort, packetParser, this);

        textView.append("Start server socket\n");

        connection.addNewClientEvent(new WiFiConnection.OnNewClientEvent() {
            @Override
            public void onConnect(Socket socket) {
                //textView.append("Client connected\n");

                wiFiToSerialProxy.addSocket(socket);
                //textView.append("SerialProxy started\n");
            }
        });

        connection.startServer();
        wiFiToSerialProxy.start();

        /*

        */

        return 0;
    }



}
