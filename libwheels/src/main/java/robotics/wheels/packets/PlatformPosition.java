package robotics.wheels.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 16.05.2016.
 */
public class PlatformPosition extends Packet {

    private float x;
    private float y;

    public PlatformPosition() {

    }

    public PlatformPosition(RawDataPacket rawDataPacket) {
        FromRawPacket(rawDataPacket);
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(x);
        buffer.putFloat(y);
        return buffer.array();
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.x = buffer.getFloat();
        this.y = buffer.getFloat();
    }
}
