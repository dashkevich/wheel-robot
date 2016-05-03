package robotics.wheeltestpatefon.Packets;

/**
 * Created by Бацька on 01.05.2016.
 */

abstract public class Packet {


    public enum WheelsRobotWiFiPacketType {

        MotorsPower(1),
        PlatformParameters(2),
        Telemetry(3),
        Settings(4),
        GetSettings(5);

        private int value;

        WheelsRobotWiFiPacketType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum WheelsRobotUsartPacketType {

        MotorsPower(1),
        PlatformParameters(2),
        Telemetry(3),
        Settings(4),
        Ping(5);

        private int value;

        WheelsRobotUsartPacketType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    public Packet(){

    }


    abstract public void FromRawPacket(RawDataPacket rawDataPacket);
    abstract public byte[] ToByteArray();

}
