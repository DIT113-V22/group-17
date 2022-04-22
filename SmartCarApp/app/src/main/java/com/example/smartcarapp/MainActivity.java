package com.example.smartcarapp;

import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void forward(View view) {System.out.println("f");}
    public void reverse(View view) {System.out.println("b");}
    public void turnRight(View view) {System.out.println("r");}
    public void turnLeft(View view) {System.out.println("l");}
    public void stopCar(View view) {System.out.println("s");}
}