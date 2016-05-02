package robotics.wheeltest.Packets;

/**
 * Created by Бацька on 28.04.2016.
 */


public class RawDataPacket {

    private byte data[];
    private int type;

    public RawDataPacket(byte[] data){
        this.data = data;
    }

    public RawDataPacket(){

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }


    public void setData(byte[] data) {
        this.data = data;
    }

}