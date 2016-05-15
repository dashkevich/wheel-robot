package robotics.wheels.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import robotics.wheels.R;
import robotics.wheels.packets.ControlPacket;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView resultTextView;
    private Joystick joystick;
    private SeekBar seekbar;
    private EditText ipAddressEdit, portEdit, headingEdit, editTextVM, editTextWM;
    private Button connectButton, resetButton, settingsButton;
    private CheckBox headingCheckBox;

    private SensorManager sensorManager;
    private Sensor gsensor;
    private Sensor msensor;

    private float[] aValues = new float[3];
    private float[] mValues = new float[3];
    public float azimuth = 0f;

    private final Socket socket = new Socket();
    private OutputStream outputStream;

    private float w = 0.0f, vx = 0.0f, vy = 0.0f;
    private final Timer timer = new Timer();
    private boolean connected = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_main, null);

        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(customView);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        resultTextView = (TextView) findViewById(R.id.textView);
        ipAddressEdit = (EditText) findViewById(R.id.ipAddr);
        portEdit = (EditText) findViewById(R.id.port);
        headingEdit = (EditText) findViewById(R.id.headingEditText);
        editTextVM = (EditText) findViewById(R.id.editTextVxVy);
        editTextWM = (EditText) findViewById(R.id.editTextW);

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vx = vy = w = 0.0f;
                seekbar.setProgress(50);
            }
        });

        headingCheckBox = (CheckBox) findViewById(R.id.headingCheckBox);
        headingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                headingEdit.setEnabled(isChecked);
            }
        });

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ipAddress = ipAddressEdit.getText().toString();
                final Integer port = Integer.valueOf(portEdit.getText().toString());

                if (!connected) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket.connect(new InetSocketAddress(ipAddress, port));
                                outputStream = socket.getOutputStream();

                                timer.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        try {
                                            outputStream.write(getPlatformParametersPacket());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 1000, 100);

                                connected = true;

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resultTextView.setText("connected");
                                        connectButton.setText("d");
                                        unFreezeControls();
                                    }
                                });

                            } catch (IOException e) {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            closeDataSending();
                                            freezeControls();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                        resultTextView.setText("can't establish connection");
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    try {
                        closeDataSending();
                        freezeControls();
                    } catch (IOException e) {
                        resultTextView.setText("can't close connection");
                        e.printStackTrace();
                    }
                }
            }
        });

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                w = (progress / 100.0f - 0.5f) * 2.0f * (float) Math.PI;
                Log.i("prog", Float.toString(w));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {

            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                vx = (float) Math.cos(degrees * Math.PI / 180f) * offset;
                vy = (float) Math.sin(degrees * Math.PI / 180f) * offset;

                resultTextView.setText(String.format("%.2f", vx) + " " + String.format("%.2f", vy));
            }

            @Override
            public void onUp() {
                vx = vy = 0.0f;
            }
        });

        freezeControls();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType ()){
            case Sensor.TYPE_ACCELEROMETER:
                aValues = event.values.clone ();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = event.values.clone ();
                break;
        }

        float[] R = new float[16];
        float[] R2 = new float[16];

        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrix (R, null, aValues, mValues);
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R2);
        SensorManager.getOrientation (R2, orientationValues);
        azimuth = orientationValues[0];
                if(!headingCheckBox.isChecked())
                    headingEdit.setText(String.format("%.2f", Math.toDegrees(azimuth)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    private byte[] getPlatformParametersPacket() {

        if(headingCheckBox.isChecked()) {
            try {
                float f = Float.parseFloat(headingEdit.getText().toString());

                if(f != 0.0f) {
                    float[][] m = new float[][]{
                            {(float) Math.cos(f), (float) Math.sin(f)},
                            {-(float) Math.sin(f), (float) Math.cos(f)}
                    };

                    float x = vx, y = vy;
                    vx = m[0][0] * x + m[0][1] * y;
                    vy = m[1][0] * x + m[1][1] * y;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        float vmult = Float.valueOf(editTextVM.getText().toString());
        float wmult = Float.valueOf(editTextWM.getText().toString());

        return new ControlPacket(vx * vmult, vy * vmult, w * wmult).ToByteArray();
    }

    private void freezeControls() {
        joystick.setEnabled(false);
        seekbar.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void unFreezeControls() {
        joystick.setEnabled(true);
        seekbar.setEnabled(true);
        seekbar.setProgress(50);
        resetButton.setEnabled(true);
    }

    private void closeDataSending() throws IOException {
        timer.cancel();

        if(outputStream != null)
            outputStream.close();
        socket.close();

        resultTextView.setText("disconnected");
        connectButton.setText("c");
        connected = false;
    }
}
