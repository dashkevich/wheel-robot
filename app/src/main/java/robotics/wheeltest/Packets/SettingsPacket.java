package robotics.wheeltest.Packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 01.05.2016.
 */
public class SettingsPacket extends Packet {
    float kp, ki;
    float maxI;

    public SettingsPacket(RawDataPacket rawDataPacket) {
        this.FromRawPacket(rawDataPacket);
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(kp);
        buffer.putFloat(ki);
        buffer.putFloat(maxI);
        return buffer.array();
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.kp = buffer.getFloat();
        this.ki = buffer.getFloat();
        this.maxI = buffer.getFloat();
    }
}
