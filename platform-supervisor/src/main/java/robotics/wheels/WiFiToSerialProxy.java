package robotics.wheels;

import android.graphics.Color;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import robotics.wheels.packets.ControlPacket;
import robotics.wheels.packets.IRLidarDataPacket;
import robotics.wheels.packets.MotorsPowerPacket;
import robotics.wheels.packets.Packet;
import robotics.wheels.packets.PingPacket;
import robotics.wheels.packets.PlatformPosition;
import robotics.wheels.packets.RawDataPacket;
import robotics.wheels.packets.SettingsPacket;
import robotics.wheels.packets.TextPacket;
import robotics.wheels.packets.WheelsVelocitiesPacket;

/**
 * Created by Бацька on 10.04.2016.
 */


public class WiFiToSerialProxy extends Thread{

    private List<CommunicationThread> connections;
    private SerialPort serialPort;
    private PacketParser packetParser;

    private Timer controlTimer;
    private Timer pingTimer;

    List<RawDataPacket> packetsBuffer;  //raw packets data from wifi connections

    SettingsPacket settingsPacket;

    //type of last recieved control packet;
    Packet lastControlPacket;
    Packet.WheelsRobotUsartPacketType lastControlPacketType;

    private MainActivity context;

    WiFiToSerialProxy(SerialPort port, PacketParser packetParser, MainActivity context) {
        connections = new ArrayList<>();
        serialPort = port;
        this.packetParser = packetParser;
        controlTimer = new Timer();
        pingTimer = new Timer();
        packetsBuffer = new LinkedList<>();
        this.context = context;

        //костыль 3
        lastControlPacket = new ControlPacket();
        ByteBuffer bb = ByteBuffer.allocate(12);
        bb.putFloat(0);
        bb.putFloat(0);
        bb.putFloat(0);
        lastControlPacket.FromRawPacket(new RawDataPacket(bb.array()));
        lastControlPacketType = Packet.WheelsRobotUsartPacketType.PlatformParameters;

    }

    public void addSocket(Socket s){
        synchronized (connections){
            CommunicationThread t = new CommunicationThread(s);
            connections.add(t);
            t.start();
        }
    }


    @Override
    public void run() {


        //read settings packet from file and send this to serial port
        settingsPacket = new SettingsPacketManager().readSettingsPacketFromFile();
        if(settingsPacket != null){
            sendPacketToSerialPort(settingsPacket, Packet.WheelsRobotUsartPacketType.Settings);
        }


        //запуск таймера отправки пакетов в ком порт с частотой 10Гц
        controlTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                sendPacketToSerialPort(lastControlPacket, lastControlPacketType);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.scrollView.setText(Arrays.toString(lastControlPacket.ToByteArray()));
                    }
                });
            }
        }, 1000 , 100);


        //ping пакеты 5Гц
        pingTimer.schedule(new TimerTask() {
            PingPacket pingPacket = new PingPacket();

            @Override
            public void run() {
                serialPort.write(pingPacket.getPingPacketData());
            }
        }, 1000, 200);


        //Управляющий цикл. Если есть пакеты в буфере пакетов,
        // обрабатываем их и рассылаем кому ннада.
        while (!Thread.currentThread().isInterrupted()) {

            if(connections.size() == 0) continue;

            //извлекаем пакеты из буффера для обработки
            synchronized (packetsBuffer) {
                for (int i = 0; i < packetsBuffer.size(); i++) {

                    RawDataPacket packet = packetsBuffer.get(i);

                    if(packet.getType() == Packet.WheelsRobotWiFiPacketType.WheelsVelocities.getValue()){

                        lastControlPacket = new WheelsVelocitiesPacket(packet);
                        lastControlPacketType = Packet.WheelsRobotUsartPacketType.WheelsVelocities;

                    }else if (packet.getType() == Packet.WheelsRobotWiFiPacketType.PlatformParameters.getValue()) {

                        lastControlPacket = new ControlPacket(packet);
                        lastControlPacketType = Packet.WheelsRobotUsartPacketType.PlatformParameters;

                    }else if(packet.getType() == Packet.WheelsRobotWiFiPacketType.MotorsPower.getValue()){

                        lastControlPacket = new MotorsPowerPacket(packet);
                        lastControlPacketType = Packet.WheelsRobotUsartPacketType.MotorsPower;

                    }else if(packet.getType() == Packet.WheelsRobotWiFiPacketType.PlatformPosition.getValue()){

                        lastControlPacket = new PlatformPosition(packet);
                        lastControlPacketType = Packet.WheelsRobotUsartPacketType.PlatformPosition;

                    }else if(packet.getType() == Packet.WheelsRobotWiFiPacketType.Settings.getValue()){

                        //save packet to file and send to serial port
                        settingsPacket = new SettingsPacket(packet);
                        SettingsPacketManager settingsPacketManager = new SettingsPacketManager();
                        if( !settingsPacketManager.saveSettingsPacketToFile(settingsPacket) ){
                            //saving settings error!
                            context.textView3.setText("save error");
                        }

                        sendPacketToSerialPort(settingsPacket, Packet.WheelsRobotUsartPacketType.Settings);

                    }else if(packet.getType() == Packet.WheelsRobotWiFiPacketType.GetSettings.getValue()){

                        //send current settings to wifi
                        if(settingsPacket != null) {
                            sendPacketToWiFi(new RawDataPacket(settingsPacket.ToByteArray()), Packet.WheelsRobotWiFiPacketType.Settings);
                        }

                    }else if(packet.getType() == Packet.WheelsRobotWiFiPacketType.Text.getValue()){

                        sendPacketToSerialPort(new TextPacket(packet), Packet.WheelsRobotUsartPacketType.Text);

                    }

                    packetsBuffer.remove(i);
                }
            }

            //считывание пакетов из сериал порта и отправка всем
            int count = packetParser.packetsAvailable();
            if(count > 0){
                for(int i=0; i<count; i++){

                    RawDataPacket packet = packetParser.getPacket();
                    if(packet.getType() == Packet.WheelsRobotUsartPacketType.Telemetry.getValue()) {
                        sendPacketToWiFi(packet, Packet.WheelsRobotWiFiPacketType.Telemetry);
                    }else if(packet.getType() == Packet.WheelsRobotUsartPacketType.Text.getValue()){
                        sendPacketToWiFi(packet, Packet.WheelsRobotWiFiPacketType.Text);

                    }else if(packet.getType() == Packet.WheelsRobotUsartPacketType.IRLidarData.getValue()){

                        IRLidarDataPacket irld = new IRLidarDataPacket(packet);
                        LineData data = context.mChart.getData();

                        if(data != null) {
                            ILineDataSet set = data.getDataSetByIndex(0);
                            if (set == null) {
                                set = new LineDataSet(null, "Dynamic Data");
                                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                                set.setValueTextColor(Color.WHITE);
                                set.setValueTextSize(9f);
                                set.setDrawValues(false);

                                data.addDataSet(set);
                            }
                            data.addXValue(irld.getAng() + "");
                            data.addEntry(new Entry(irld.getDist(), set.getEntryCount()), 0);

                            // let the chart know it's data has changed
                            context.mChart.notifyDataSetChanged();
                            context.mChart.setVisibleXRangeMaximum(6);
                            context.mChart.setVisibleYRangeMaximum(15, YAxis.AxisDependency.LEFT);
                            context.mChart.moveViewTo(data.getXValCount() - 7, 50f, YAxis.AxisDependency.LEFT);
                        }
                    }
                }
            }

        }

        controlTimer.cancel();
        pingTimer.cancel();
    }



    private void sendPacketToSerialPort(Packet packet, Packet.WheelsRobotUsartPacketType type){

        byte[] packetData = packet.ToByteArray();
        ByteBuffer bb = ByteBuffer.allocate(4+1+1+packetData.length);

        bb.put("+IPD".getBytes());
        bb.put((byte) (packetData.length+1));
        bb.put((byte)type.getValue());
        bb.put(packetData);

        serialPort.write(bb.array());
    }

    //send packets from serial port to all wifi connections
    private void sendPacketToWiFi(RawDataPacket packet, Packet.WheelsRobotWiFiPacketType type){

        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {

                byte[] data = packet.getData();
                ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + data.length);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.putInt(data.length + 1);
                buffer.put((byte) type.getValue());
                buffer.put(data);

                //if connection has been closed or any other error - close this connection
                if (!connections.get(i).write(buffer.array())) {
                    connections.get(i).interrupt(); //stop thread
                    connections.remove(i); //remove connection
                }
            }
        }
    }





    class CommunicationThread extends Thread {

        private Socket clientSocket;

        private InputStream in;
        private OutputStream out;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                in = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();
            } catch (IOException e){
                return;
            }
        }

        public boolean write(byte[] data){
            try {
                out.write(data);
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }

            return true;
        }

        public void run() {

            byte buffer[] = new byte[1024*256];


            int n = 0;
            byte sizeBuff[] = new byte[4];
            int blockSize = 0;

            //прием пакетов и добавление их в буффер
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    if(blockSize == 0){
                        if(in.available() < 4){
                            continue;
                        }
                        in.read(sizeBuff,0,4);
                        //convert from c/c++ little endian to java big endian
                        blockSize = ByteBuffer.wrap(sizeBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    }

                    if(in.available() < blockSize){
                        continue;
                    }


                    n = in.read(buffer, 0, blockSize);

                    //create raw data packet
                    byte[] data = new byte[n-1];
                    System.arraycopy(buffer,1,data,0,n-1);
                    RawDataPacket rawDataPacket = new RawDataPacket();
                    rawDataPacket.setType(buffer[0]);
                    rawDataPacket.setData(data);


                    synchronized (packetsBuffer){
                        packetsBuffer.add(rawDataPacket);
                    }

                    blockSize = 0;


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                in.close();
                out.close();
                clientSocket.close();
            }catch (IOException e){

            }
        }
    }

    class SettingsPacketManager{

        private final String filename = "settings_packet.bin";

        public boolean saveSettingsPacketToFile(SettingsPacket packet){

            try {
                FileOutputStream fileOutputStream = context.openFileOutput(filename, context.MODE_PRIVATE);

                fileOutputStream.write(packet.ToByteArray());

                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                return false;
            }catch(IOException e){
                return false;
            }
            return true;
        }

        public  SettingsPacket readSettingsPacketFromFile(){

            SettingsPacket settingsPacket = null;

            try {
                FileInputStream fileInputStream = context.openFileInput(filename);

                if(fileInputStream.available() > 0){
                    byte[] buffer = new byte[fileInputStream.available()];
                    fileInputStream.read(buffer);
                    settingsPacket = new SettingsPacket(new RawDataPacket(buffer));
                }

                fileInputStream.close();
            } catch (IOException e) {

            }

            return settingsPacket;
        }
    }

}