package robotics.wheels.packets;

/**
 * Created by Бацька on 01.05.2016.
 */

abstract public class Packet {


    public enum WheelsRobotWiFiPacketType {

        MotorsPower(1),
        WheelsVelocities(2),
        PlatformParameters(3),
        Telemetry(4),
        Settings(5),
        GetSettings(6),
        Text(7),
        IRLidarData(8),
        PlatformPosition(9);

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
        WheelsVelocities(2),
        PlatformParameters(3),
        Telemetry(4),
        Settings(5),
        Ping(6),
        Text(7),
        IRLidarData(8),
        PlatformPosition(9);


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
