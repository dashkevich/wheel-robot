package robotics.wheeltestpatefon.Packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 01.05.2016.
 */
public class MotorsPowerPacket extends Packet {

    private float powerA, powerB, powerC, powerD;

    public MotorsPowerPacket(RawDataPacket rawDataPacket) {
        this.FromRawPacket(rawDataPacket);
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(powerA);
        buffer.putFloat(powerB);
        buffer.putFloat(powerC);
        buffer.putFloat(powerD);
        return buffer.array();
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.powerA = buffer.getFloat();
        this.powerB = buffer.getFloat();
        this.powerC = buffer.getFloat();
        this.powerD = buffer.getFloat();
    }
}
