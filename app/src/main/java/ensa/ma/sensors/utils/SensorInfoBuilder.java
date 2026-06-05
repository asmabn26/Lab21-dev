package ensa.ma.sensors.utils;

import android.hardware.Sensor;

public class SensorInfoBuilder {

    public static String buildCardText(Sensor sensor) {
        return "Capteur ID : " + sensor.getId() + "\n"
                + "Nom : " + sensor.getName() + "\n"
                + "Fabricant : " + sensor.getVendor() + "\n"
                + "Version : " + sensor.getVersion() + "\n"
                + "Type texte : " + sensor.getStringType() + "\n"
                + "Type entier : " + sensor.getType() + "\n"
                + "Résolution : " + sensor.getResolution() + "\n"
                + "Consommation : " + sensor.getPower() + " mA\n"
                + "Plage maximale : " + sensor.getMaximumRange() + "\n"
                + "Délai minimum : " + sensor.getMinDelay() + " µs\n";
    }
}