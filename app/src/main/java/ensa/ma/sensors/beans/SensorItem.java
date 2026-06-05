package ensa.ma.sensors.beans;

public class SensorItem {

    public String id;
    public String name;
    public String type;
    public String vendor;
    public String version;
    public String resolution;
    public String range;
    public String power;
    public String max_speed;

    public SensorItem(
            String id,
            String name,
            String type,
            String vendor,
            String version,
            String resolution,
            String range,
            String power,
            String max_speed
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.vendor = vendor;
        this.version = version;
        this.resolution = resolution;
        this.range = range;
        this.power = power;
        this.max_speed = max_speed;
    }
}