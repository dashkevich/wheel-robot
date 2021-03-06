package robotics.wheels;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.LinkedList;
import java.util.List;

import robotics.wheels.packets.RawDataPacket;


/**
 * Created by Бацька on 11.04.2016.
 */
public class PacketParser implements SerialInputOutputManager.Listener {

    SerialPort serialPort;
    byte buffer[] = new byte[1024*256];
    int position = 0;
    byte packet[];


    private List<RawDataPacket> packets;

    PacketParser(SerialPort serialPort){
        packets = new LinkedList<>();

        this.serialPort = serialPort;
        serialPort.addReadListener(this);
    }

    public synchronized int packetsAvailable(){
        return packets.size();
    }

    public RawDataPacket getPacket(){

        RawDataPacket pd;
        synchronized (packets) {
            pd = packets.get(0);
            packets.remove(0);
        }
        return pd;
    }

    @Override
    public void onNewData(byte[] data) {
        if(data.length < (buffer.length-position+1)){
            for (int i=0; i<data.length; i++){
                buffer[position] = data[i];
                ++position;
            }
        }

        //если найден пакет с даными
        if (parse()) {
            synchronized (packets) {
                RawDataPacket rawDataPacket = new RawDataPacket();

                byte[] b = new byte[packet.length-1];
                System.arraycopy(packet,1,b,0,packet.length-1);

                rawDataPacket.setData(b);
                rawDataPacket.setType(packet[0]);
                packets.add(rawDataPacket);
            }
        }
    }

    @Override
    public void onRunError(Exception e) {

    }

    private boolean parse(){
        //
        int start = findStartPosition();
        if(start == -1){
            return false;
        }

        //проверка байта размера
        int size = 0;
        if(start+4 < position){
            size = buffer[start+4];
        }else {
            return false;
        }

        //если пакет найден
        if( (position - (start+4+1)) >= size){
            packet = new byte[size];

            //копируем пакет
            for(int i=0, j=start+5; i<size; i++){
                packet[i] = buffer[j++];
            }

            //сместить буфер
            int i = start+4+size+1;
            for(int j=0; i<position; i++,j++){
                buffer[j] = buffer[i];
            }

            position = position - (start+4+size+1);

            if(position < 0){
                position = 0;
            }

            return true;
        }

        return false;
    }


    private int findStartPosition(){
        for(int i=0; i<position-4; i++){
            if(buffer[i] == '+'){
                if(buffer[i+1] == 'I' && buffer[i+2] == 'P' && buffer[i+3] == 'D'){
                        return i;
                }
            }
        }

        return -1;
    }


}
