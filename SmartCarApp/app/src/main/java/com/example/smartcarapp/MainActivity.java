package com.example.smartcarapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SmartCarApp";
    private static final String LOCALHOST = "10.0.2.2";
    private static final String MQTT_SERVER = "tcp://" + LOCALHOST + ":1883";
    private static final int QOS = 1;
    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;

    public static MqttClient mMqttClient;
    private boolean isConnected = false;
    public static ImageView mCameraView;
    TextView txtView;
    public ImageView mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMqttClient = new MqttClient(getApplicationContext(), MQTT_SERVER, TAG);
        mCameraView = findViewById(R.id.imageView);
        connectToMqttBroker();
        mic = findViewById(R.id.speechMic);
        mic.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectToMqttBroker();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMqttClient.disconnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "Disconnected from broker");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            }
        });
    }

    private void connectToMqttBroker() {
        if (!isConnected) {
            mMqttClient.connect(TAG, "", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnected = true;
                    final String successfulConnection = "Connected to MQTT broker";
                    Log.i(TAG, successfulConnection);
                    Toast.makeText(getApplicationContext(), successfulConnection, Toast.LENGTH_SHORT).show();



                    mMqttClient.subscribe("/smartcar/ultrasound/front", QOS, null);
                    mMqttClient.subscribe("/smartcar/camera", QOS, null);
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
                public void messageArrived(String topic, MqttMessage message) {
                    if (topic.equals("/smartcar/camera")) {
                        final Bitmap bm = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);

                        final byte[] payload = message.getPayload();
                        final int[] colors = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
                        for (int ci = 0; ci < colors.length; ++ci) {
                            final byte r = payload[3 * ci];
                            final byte g = payload[3 * ci + 1];
                            final byte b = payload[3 * ci + 2];
                            colors[ci] = Color.rgb(r, g, b);
                        }
                        bm.setPixels(colors, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                        mCameraView.setImageBitmap(bm);
                    } else {
                        Log.i(TAG, "[MQTT] Topic: " + topic + " | Message: " + message.toString());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivered");
                }
            });
        }
    }


    void drive(String topic,String message,String actionDescription){
        if (!isConnected) {
            final String notConnected = "Not connected (yet)";
            Log.e(TAG, notConnected);
            Toast.makeText(getApplicationContext(), notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, actionDescription);
        mMqttClient.publish(topic,message,QOS,null);
    }


    public void reverse(View view) {
        drive("myfirst/test","b","Moving backward");
    }

    public void turnRight(View view) {
        drive("myfirst/test","r","Turning right");
    }

    public void turnLeft(View view) {
        drive("myfirst/test","l","Turning left");
    }

   public void forward(View view) {
       drive("myfirst/test","f","Moving forward");
    }

    public void stopCar(View view) {
        drive("myfirst/test","s","Stopping");
    }

    public void playAudio(int AudioFile){
        final MediaPlayer mp3 = MediaPlayer.create(this, AudioFile);
        CountDownTimer cntr_aCounter = new CountDownTimer(900, 1000) {
            @Override
            public void onTick(long l) {
                mp3.start();
            }

            @Override
            public void onFinish() {
                mp3.stop();
            }

        };cntr_aCounter.start();
    }

    private String message;
    private int audioPath;

    public  void speechCommands(int requestCode, int resultCode, Intent data){
        if (requestCode==10 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String stringResult = result.get(0);
            if (stringResult.contains("forward")) {
                message = "f";
                audioPath = R.raw.going_forward;
            } else if (stringResult.contains("backward")) {
                audioPath = R.raw.going_backward;
                message = "b";
            } else if (stringResult.contains("left")) {
                audioPath = R.raw.turning_left;
                message = "l";
            } else if (stringResult.contains("right")) {
                audioPath = R.raw.turning_right;
                message = "r";
            } else if (stringResult.contains("stop")) {
                audioPath = R.raw.stopping_car;
                message = "s";
            }else{
                audioPath = 5;
                message = "";
            }

            CountDownTimer cntr_aCounter = new CountDownTimer(150, 1000) {

                @Override
                public void onTick(long l) {
                    //200 iq if statement
                    if(audioPath!=5){
                        playAudio(audioPath);
                    }
                }

                @Override
                public void onFinish() {
                    if(!message.equals("")){
                        mMqttClient.publish("myfirst/test", message, 1, null);
                    }
                }

            };cntr_aCounter.start();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        speechCommands(requestCode,resultCode,data);
    }
}