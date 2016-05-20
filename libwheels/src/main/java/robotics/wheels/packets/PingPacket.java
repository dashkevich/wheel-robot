package robotics.wheels.packets;

import java.nio.ByteBuffer;

/**
 * Created by Бацька on 17.05.2016.
 */
public class PingPacket{

    ByteBuffer data;

    public PingPacket(){
        data = ByteBuffer.allocate(6);
        data.put("+IPD".getBytes()); // header
        data.put((byte)(1)); //data size
        data.put((byte) Packet.WheelsRobotUsartPacketType.Ping.getValue()); //data type
    }

    public byte[] getPingPacketData() {

        return data.array();
    }

}
