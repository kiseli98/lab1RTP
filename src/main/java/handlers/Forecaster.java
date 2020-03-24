package handlers;

import model.MessageSensor1;
import model.Sensor;

import java.util.concurrent.CopyOnWriteArrayList;

public class Forecaster implements Handler {
    CopyOnWriteArrayList<Sensor> sensorEntries = new CopyOnWriteArrayList<>();
    private double forecastInterval;
    long flag = 0;

    public Forecaster(double interval) {
        forecastInterval = interval;
    }

    @Override
    public void receive(Object msg) {
        if (msg == null) {
            return;
        }

        if (sensorEntries.isEmpty()) {
            flag = System.currentTimeMillis();
        }

        sensorEntries.add((Sensor) msg);
        if (System.currentTimeMillis() - flag < forecastInterval * 1000) {
            return;
        }


        String forecastSensors = msg instanceof MessageSensor1 ? "Forecast from sensor 1 : " : "Forecast from sensor 2 : ";


        double temperatureSensor = 0;
        double atmoPressureSensor = 0;
        double humiditySensor = 0;
        double lightSensor = 0;
        double windSpeedSensor = 0;

        for (Sensor sensor : sensorEntries) {
            temperatureSensor += sensor.getTemperatureSensor();
            atmoPressureSensor += sensor.getAtmoPressureSensor();
            humiditySensor += sensor.getHumiditySensor();
            lightSensor += sensor.getLightSensor();
            windSpeedSensor += sensor.getWindSpeedSensor();
        }

        temperatureSensor /= sensorEntries.size();
        atmoPressureSensor /= sensorEntries.size();
        humiditySensor /= sensorEntries.size();
        lightSensor /= sensorEntries.size();
        windSpeedSensor /= sensorEntries.size();

        Sensor message = new MessageSensor1(
                temperatureSensor,
                humiditySensor,
                windSpeedSensor,
                atmoPressureSensor,
                lightSensor
        );

        String forecast = doForecast(message);
        sensorEntries.clear();
        system.sendMessage("printer",
                forecastSensors + forecast +
                        "\n Temperature - " + message.getTemperatureSensor() +
                        "\n Humidity - " + message.getHumiditySensor() +
                        "\n Atmosphere pressure - " + message.getAtmoPressureSensor() +
                        "\n Light - " + message.getLightSensor() +
                        "\n Wind speed - " + message.getWindSpeedSensor()
        );
    }

    private static String doForecast(Sensor message) {
        String forecast = "JUST_A_NORMAL_DAY";

        if (message.getTemperatureSensor() < -2 &&
                message.getLightSensor() < 128 &&
                message.getAtmoPressureSensor() < 720) {
            forecast = "SNOW";
        }

        if (message.getTemperatureSensor() < -2 &&
                message.getLightSensor() > 128 &&
                message.getAtmoPressureSensor() < 680) {
            forecast = "WET_SNOW";
        }

        if (message.getTemperatureSensor() < -8) {
            forecast = "SNOW";
        }

        if (message.getTemperatureSensor() < -15 &&
                message.getWindSpeedSensor() > 45) {
            forecast = "BLIZZARD";
        }

        if (message.getTemperatureSensor() > 0 &&
                message.getAtmoPressureSensor() < 710 &&
                message.getHumiditySensor() > 70 &&
                message.getWindSpeedSensor() < 20) {
            forecast = "SLIGHT_RAIN";
        }

        if (message.getTemperatureSensor() > 0 &&
                message.getAtmoPressureSensor() < 690 &&
                message.getHumiditySensor() > 70 &&
                message.getWindSpeedSensor() > 20) {
            forecast = "HEAVY_RAIN";
        }

        if (message.getTemperatureSensor() > 30 &&
                message.getAtmoPressureSensor() < 770 &&
                message.getHumiditySensor() > 80 &&
                message.getLightSensor() > 192) {
            forecast = "HOT";
        }


        if (message.getTemperatureSensor() > 30 &&
                message.getAtmoPressureSensor() < 770 &&
                message.getHumiditySensor() > 50 &&
                message.getLightSensor() > 192 &&
                message.getWindSpeedSensor() > 35) {
            forecast = "CONVECTION_OVEN";
        }

        if (message.getTemperatureSensor() > 25 &&
                message.getAtmoPressureSensor() < 750 &&
                message.getHumiditySensor() > 70 &&
                message.getLightSensor() < 192 &&
                message.getWindSpeedSensor() < 10) {
            forecast = "CONVECTION_OVEN";
        }

        if (message.getTemperatureSensor() > 25 &&
                message.getAtmoPressureSensor() < 750 &&
                message.getHumiditySensor() > 70 &&
                message.getLightSensor() < 192 &&
                message.getWindSpeedSensor() > 10
        ) {
            forecast = "SLIGHT_BREEZE";
        }
        if (message.getLightSensor() < 128) {
            forecast = "CLOUDY";
        }

        if (message.getTemperatureSensor() > 30 &&
                message.getAtmoPressureSensor() < 660 &&
                message.getHumiditySensor() > 85 &&
                message.getWindSpeedSensor() > 45) {
            forecast = "MONSOON";
        }

        return forecast;
    }

}
