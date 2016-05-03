package robotics.wheeltestpatefon.Packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Бацька on 01.05.2016.
 */
public class ControlPacket extends Packet{
    private float vx;
    private float vy;
    private float w;

    public ControlPacket(){

    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    @Override
    public void FromRawPacket(RawDataPacket rawDataPacket) {
        //raw data consist little endian bytes
        ByteBuffer buffer = ByteBuffer.wrap(rawDataPacket.getData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.vx = buffer.getFloat();
        this.vy = buffer.getFloat();
        this.w = buffer.getFloat();
    }

    @Override
    public byte[] ToByteArray() {

        //convert java big endian to c/c++ little endian
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(vx);
        buffer.putFloat(vy);
        buffer.putFloat(w);
        return buffer.array();
    }
}
