package robotics.wheeltest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.util.HexDump;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import java.nio.ByteBuffer;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private WiFiConnection connection;
    private SerialPort serialPort;
    private PacketParser packetParser;
    private TextView result;

    private Joystick joystick;

    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    private void updateLog(String message) {
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        result = (TextView) findViewById(R.id.textView);
        joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                float vx = (float) Math.cos(degrees * Math.PI / 180f) * offset;
                float vy = (float) Math.sin(degrees * Math.PI / 180f) * offset;

                result.setText(Float.toString(vx) + "     " + Float.toString(vy));
                serialPort.write(getPlatformParametersPacket(vx,vy));
            }

            @Override
            public void onUp() {
            }
        });

        serialPort = new SerialPort(this);
        packetParser = new PacketParser(serialPort);

        if(!serialPort.connect()){
            return;
        }

        serialPort.startIoManager();
    }


    private byte[] getPlatformParametersPacket(float vx, float vy /* w */) {
        ByteBuffer bb = ByteBuffer.allocate(70);

        bb.put("+IPD".getBytes());
        bb.put((byte)(4 * 3 + 1));
        bb.put((byte)1);
        bb.putInt(Float.floatToIntBits(vx));
        bb.putInt(Float.floatToIntBits(vy));
        // TODO put w bb.putFloat(w);
        bb.putInt(Float.floatToIntBits(0.0f));


        /*byte buffer[] = {'+', 'I', 'P', 'D', 1};
        bb.put(buffer);
        bb.put(Integer.toString(4 * 3 + 1).getBytes());

*/
        return bb.array();
    }
}
