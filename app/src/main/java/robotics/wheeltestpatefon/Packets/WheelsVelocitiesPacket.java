package robotics.wheeltestpatefon.Packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 09.05.2016.
 */
public class WheelsVelocitiesPacket extends Packet {

    float v1, v2, v3, v4;

    public WheelsVelocitiesPacket(){

    }

    public WheelsVelocitiesPacket(RawDataPacket rawDataPacket){
        FromRawPacket(rawDataPacket);
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.v1 = buffer.getFloat();
        this.v2 = buffer.getFloat();
        this.v3 = buffer.getFloat();
        this.v4 = buffer.getFloat();
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(v1);
        buffer.putFloat(v2);
        buffer.putFloat(v3);
        buffer.putFloat(v4);
        return buffer.array();
    }
}
