package com.example.smartcarapp;

public class SpeechToText {
    //this class is for processing speech commands
    public static String speechCommands(String result){
        String mqttMessage;
        if (result.contains("forward")) {
            mqttMessage = "f";
            HomeFragment.audioPath = R.raw.going_forward;
        } else if (result.contains("backward")) {
            HomeFragment.audioPath = R.raw.going_backward;
            mqttMessage = "b";
        } else if (result.contains("left")) {
            HomeFragment.audioPath = R.raw.turning_left;
            mqttMessage = "l";
        } else if (result.contains("right")) {
            HomeFragment.audioPath = R.raw.turning_right;
            mqttMessage = "r";
        } else if (result.contains("stop")) {
            HomeFragment.audioPath = R.raw.stopping_car;
            mqttMessage = "s";
        }else{
            HomeFragment.audioPath = 5;
            mqttMessage = "";
        }
        return mqttMessage;
    }
}
