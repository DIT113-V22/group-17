package com.example.smartcarapp;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigDecimal;

//This class has simple functions like truncation and stuff
public class UsefulFunctions {
    //takes the string, converts it into a double and then truncates it
    public static  double truncateNumber(MqttMessage result){
        double value = Double.parseDouble(result.toString());
        value = value * 100.0;
        int temp = (int) value;
        value = temp / 100.0;
        return value;
    }

    public static String convertToKM(Double distance){
        BigDecimal distanceInKm = new BigDecimal(distance / 1000);
        distanceInKm = distanceInKm.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.valueOf(distanceInKm);
    }

}
