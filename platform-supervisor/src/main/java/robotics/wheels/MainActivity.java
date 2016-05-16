package robotics.wheels;

import android.app.Activity;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.net.Socket;

import robotics.wheels.communication.WiFiConnection;

public class MainActivity extends Activity implements OnChartValueSelectedListener {

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

    public LineChart mChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        // add empty data
        mChart.setData(new LineData());

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);




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

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
