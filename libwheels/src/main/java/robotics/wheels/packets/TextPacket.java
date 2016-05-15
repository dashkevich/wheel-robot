package robotics.wheels.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 09.05.2016.
 */
public class TextPacket extends Packet {

    byte[] text;

    public byte[] getText() {
        return text;
    }

    public TextPacket() {

    }

    public TextPacket(RawDataPacket rawDataPacket){
        FromRawPacket(rawDataPacket);
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        text = new byte[buffer.array().length];
        buffer.get(text);
    }

    @Override
    public byte[] ToByteArray() {
        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(text.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(text);
        return buffer.array();
    }


}
