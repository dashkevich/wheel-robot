package robotics.wheeltest;

/**
 * Created by Бацька on 28.04.2016.
 */


public class DataPacket{

    private int MAX_PACKET_SIZE = 1024*2;
    private byte data[];
    private int type;
    private int dataSize;

    DataPacket(){
        data = new byte[MAX_PACKET_SIZE];
        dataSize = 0;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        byte[] kostyl = new byte[dataSize];
        for(int i=0; i<dataSize; i++){
            kostyl[i] = data[i];
        }
        return kostyl;
    }


    public void setData(byte[] data) {
        this.data = data;
        this.dataSize = data.length;
    }

    /**
     * create new copy of input data array
     * @param data
     */
    public void copyData(byte[] data){
        dataSize = data.length;
        for (int i=0; i<data.length; i++){
            this.data[i] = data[i];
        }
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }
}