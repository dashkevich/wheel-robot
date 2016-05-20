package robotics.wheels.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 16.05.2016.
 */

public class IRLidarDataPacket extends Packet {

    private float ang;
    private float dist;

    public float getAng() {
        return ang;
    }

    public void setAng(float ang) {
        this.ang = ang;
    }

    public float getDist() {
        return dist;
    }

    public void setDist(float dist) {
        this.dist = dist;
    }

    public IRLidarDataPacket(){

    }

    public IRLidarDataPacket(RawDataPacket rawDataPacket){
        FromRawPacket(rawDataPacket);
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.ang = buffer.getFloat();
        this.dist = buffer.getFloat();
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(ang);
        buffer.putFloat(dist);
        return buffer.array();
    }
}
