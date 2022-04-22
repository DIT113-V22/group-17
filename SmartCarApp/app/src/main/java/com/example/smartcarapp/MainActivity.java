package com.example.smartcarapp;

import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SmartCarApp";
    private static final String EXTERNAL_MQTT_BROKER = "aerostun.dev";
    private static final String LOCALHOST = "10.0.2.2";
    private static final String MQTT_SERVER = "tcp://" + LOCALHOST + ":1883";
    private MqttClient mMqttClient;
    private boolean isConnected = false;
    private static final int QOS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMqttClient = new MqttClient(getApplicationContext(), MQTT_SERVER, TAG);
        connectToMqttBroker();
    }


    public void forward(View view) throws MqttException {
        String broker = "tcp://localhost:1883";
        String topicName = "test/topic";
        int qos = 1;

        MqttClient mqttClient = new MqttClient(broker, String.valueOf(System.nanoTime()));
//Mqtt ConnectOptions is used to set the additional features to mqtt message

        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true); //no persistent session
        connOpts.setKeepAliveInterval(1000);


        MqttMessage message = new MqttMessage("Ed Sheeran".getBytes());
        message.setQos(qos);     //sets qos level 1
        message.setRetained(true); //sets retained message

        MqttTopic topic2 = mqttClient.getTopic(topicName);

        mqttClient.connect(connOpts); //connects the broker with connect options
        topic2.publish(message);    // publishes the message to the topic(test/topic)
        System.out.println("f");
    }

    public void reverse(View view) {
        System.out.println("b");
    }

    public void turnRight(View view) {
        System.out.println("r");
    }

    public void turnLeft(View view) {
        System.out.println("l");
    }

    public void stopCar(View view) {
        System.out.println("s");
    }


    private void connectToMqttBroker() {
        if (!isConnected) {
            mMqttClient.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnected = true;

                    final String successfulConnection = "Connected to MQTT broker";
                    Log.i(TAG, successfulConnection);
                    Toast.makeText(getApplicationContext(), successfulConnection, Toast.LENGTH_SHORT).show();

                    mMqttClient.subscribe("myfirst/test", QOS, null);
                    //mMqttClient.subscribe("/smartcar/camera", QOS, null);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    final String failedConnection = "Failed to connect to MQTT broker";
                    Log.e(TAG, failedConnection);
                    Toast.makeText(getApplicationContext(), failedConnection, Toast.LENGTH_SHORT).show();
                }
            }, new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    isConnected = false;

                    final String connectionLost = "Connection to MQTT broker lost";
                    Log.w(TAG, connectionLost);
                    Toast.makeText(getApplicationContext(), connectionLost, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivered");
                }
            });
        }
    }
}