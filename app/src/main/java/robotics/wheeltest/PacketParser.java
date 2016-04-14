package robotics.wheeltest;

import com.hoho.android.usbserial.util.SerialPortListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Бацька on 11.04.2016.
 */
public class PacketParser implements SerialPortListener{

    SerialPort serialPort;

    byte buffer[] = new byte[1024*256];
    int position = 0;
    byte packet[];

    class PacketData{
        private byte data[];

        PacketData(byte[] data){
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }

    private List<PacketData> packets;

    PacketParser(SerialPort serialPort){
        packets = new LinkedList<>();

        this.serialPort = serialPort;
        serialPort.addReadListener(this);
    }

    public int packetsAvailable(){
        return packets.size();
    }

    public PacketData getPacket(){
        PacketData pd = packets.get(0);
        packets.remove(0);
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


        if( parse() ){
            packets.add(new PacketData(packet));
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
