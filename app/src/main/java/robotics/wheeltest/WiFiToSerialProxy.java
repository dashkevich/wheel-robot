package robotics.wheeltest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Бацька on 10.04.2016.
 */


public class WiFiToSerialProxy extends Thread{

    private List<CommunicationThread> connections;
    private SerialPort serialPort;
    private PacketParser packetParser;
    private Timer controlTimer;
    private Timer pingTimer;
    List<DataPacket> packetsBuffer;
    DataPacket controlPacket;

    WiFiToSerialProxy(SerialPort port, PacketParser packetParser){
        connections = new ArrayList<>();
        serialPort = port;
        this.packetParser = packetParser;
        controlTimer = new Timer();
        pingTimer = new Timer();
        packetsBuffer = new LinkedList<>();
        controlPacket = new DataPacket();
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

        //запуск таймера отправки пакетов в ком порт с частотой 10Гц
        controlTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                    serialPort.write(getPlatformPacket(controlPacket.getData()));
            }
        }, 1000 , 100);

        //ping пакеты 5Гц
        pingTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                serialPort.write(getPingPacket());
            }
        }, 1000, 200);


        //Управляющий цикл. Если есть пакеты в буфере пакетов,
        // обрабатываем их и рассылаем кому ннада.
        while (!Thread.currentThread().isInterrupted()) {

            if(connections.size() == 0) continue;

            //извлекаем пакеты из буффера для обработки
            synchronized (packetsBuffer) {
                for (int i = 0; i < packetsBuffer.size(); i++) {

                    //если управляющий пакет, сохраняем его в controlPacket
                    if (packetsBuffer.get(i).getType() == 100) {
                        controlPacket.setData(packetsBuffer.get(i).getData());
                        packetsBuffer.remove(i);
                    }
                }
            }

            //считывание пакетов из сериал порта и отправка всем
            int count = packetParser.packetsAvailable();
            if(count > 0){
                for(int i=0; i<count; i++){
                    for(int j=0; j<connections.size(); j++){
                        connections.get(i).write(packetParser.getPacket().getData());
                    }
                }
            }

        }

        controlTimer.cancel();
        pingTimer.cancel();
    }

    private byte[] getPlatformPacket(byte data[]) {
        ByteBuffer bb = ByteBuffer.allocate(4+1+data.length);

        bb.put("+IPD".getBytes()); // header
        bb.put((byte) (data.length)); //data size
        bb.put(data); // [ [type(1 byte)][data(data.length-1 bytes)] ]

        return bb.array();
    }

    private byte[] getPingPacket() {
        ByteBuffer bb = ByteBuffer.allocate(4+1+2);

        bb.put("+IPD".getBytes()); // header
        bb.put((byte)(2)); //data size
        bb.put((byte)0); //data type
        bb.put((byte)1); //data

        return bb.array();
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

        public void write(byte[] data){
            try {
                //write data size (4 bytes).
                out.write(ByteBuffer.allocate(4).putInt(data.length).array());
                //write data
                out.write(data);
            }catch (IOException e){

            }
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
                        blockSize = ByteBuffer.wrap(sizeBuff).getInt();
                    }

                    if(in.available() < blockSize){
                        continue;
                    }


                    n = in.read(buffer, 0, blockSize);

                    DataPacket packet = new DataPacket();
                    packet.setType(buffer[0]);
                    packet.setDataSize(n-1);
                    byte[] data = packet.getData();
                    for (int i=1; i<n; i++){
                        data[i] = buffer[i];
                    }

                    synchronized (packetsBuffer){
                        packetsBuffer.add(packet);
                    }

                    blockSize = 0;


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}