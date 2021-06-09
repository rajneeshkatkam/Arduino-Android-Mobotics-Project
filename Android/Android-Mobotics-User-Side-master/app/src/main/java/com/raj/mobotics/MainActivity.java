package com.raj.mobotics;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements SensorEventListener,TextToSpeech.OnInitListener {


    TextToSpeech tts;
    Button openCamButton,kpSend,kiSend,kdSend,manualBotMode;
    EditText KpInput,KiInput,KdInput;

    SharedPreferences file;  ///For storing Kp Ki Kd values after app shutdown


    private String arduino_IP = "192.168.43.55";  ///Static IP for Arduino
    private int ardunio_Port = 5555;              ///Port where Arduino is listening

    private String remoteDevice_IP="192.168.43.66";  ///Remote device Ip
    private int remoteDevice_Port= 6666;        ///Remote device Listening port

    private String hotspot_IP="192.168.43.1";  ///Remote device Ip
    private int hotspot_Port= 1111;        ///Remote device Listening port

    float yawVal,pitchVal,rollVal;
    Boolean threadFlag=true;


    private OSCPortOut senderArduino, senderRemote;

    private OSCPortIn receiverRemote;

    private TextView yaw,pitch,roll,statusTextView;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(1.2f);
        tts.setPitch(0.4f);
        //tts.setVoice(Voice.LATENCY_HIGH);
        ///For Hiding the Keyboard intially
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        openCamButton = findViewById(R.id.openCamButton);
        manualBotMode=findViewById(R.id.manualBotMode);


        kpSend = findViewById(R.id.kpSend);
        kiSend = findViewById(R.id.kiSend);
        kdSend = findViewById(R.id.kdSend);

        KpInput= findViewById(R.id.KpInput);
        KiInput= findViewById(R.id.KiInput);
        KdInput= findViewById(R.id.KdInput);




        ///Retriving the values of Kp Ki Kd that were saved after the app was shutdown
        file = getSharedPreferences("save", 0);



        statusTextView=findViewById(R.id.statusTextView);
        yaw=findViewById(R.id.yaw);
        pitch=findViewById(R.id.pitch);
        roll=findViewById(R.id.roll);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);



        openCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),CameraSettingsActivity.class));

            }
        });


        manualBotMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ManualBotControllerMode.class));
            }
        });


    }



    ////////////////////////////////Text To Speech Starts//////////////////////////////////////////


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("Welcome...to...Mobotics");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }


    public void speakOut(String s) {

        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }



    ///////////////////////////////Text To Speech Ends//////////////////////////////////////////




    ///Button Click Functions

    public void sendPIDValues(View view) {

        Toast.makeText(this,String.valueOf(view.getId()),Toast.LENGTH_SHORT).show();
        sendKpKiKdPacketToRemoteDevice();
    }




    private void sendKpKiKdPacketToRemoteDevice() {

        Thread thread=new Thread() {
            @Override
            public void run() {


                if (senderRemote != null) {
                    try {
                        // Send the messages


                        ///////Send Messages with arguments a multiple of 2------Very Important

                        OSCMessage KpKiKdMessage = new OSCMessage("/KpKiKdValues");

                        KpKiKdMessage.addArgument(Float.valueOf(String.valueOf(KpInput.getText())));
                        KpKiKdMessage.addArgument(Float.valueOf(String.valueOf(KiInput.getText())));
                        KpKiKdMessage.addArgument(Float.valueOf(String.valueOf(KdInput.getText())));
                        KpKiKdMessage.addArgument(2);  //4th MessagePacket


                        senderRemote.send(KpKiKdMessage);
                        Log.i("portError","Packet Sent Remote");

                    } catch (Exception e) {

                        Log.i("portError",e.getMessage());

                        //Log.i("portError",e.toString());
                        // Error handling for some error
                    }
                }

            }
        };

        thread.start();



    }
    ///-------------------------------------------------------------------------------------------




    ///Sensor Changing Value

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        yawVal =Math.round(sensorEvent.values[0]);
        pitchVal =Math.round(sensorEvent.values[1]);
        rollVal =Math.round(sensorEvent.values[2]);

        yaw.setText(String.valueOf(yawVal));
        pitch.setText(String.valueOf(pitchVal));
        roll.setText(String.valueOf(rollVal));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    ////--------------------------------------------------------------


    ///Packet Sending Function

    private void sendPacketsToArduino() {

        //Log.i("portError","Packet Called");

            if (senderArduino != null) {
                try {

                    ///////Send Messages with arguments a multiple of 2------Very Important

                    OSCMessage message = new OSCMessage("/motorValues");
                    message.addArgument(Float.valueOf(KiInput.getText().toString())*yawVal); //Left Motor PWM Value
                    message.addArgument(Float.valueOf(KiInput.getText().toString())*yawVal); //Right Motor PWM Value


                    message.addArgument(0); //Left motor direction pin one  ---- pin1-1 and pin2-0  Left Motor Backwards
                    message.addArgument(1); //Left motor direction pin two

                    message.addArgument(0); //Right motor direction pin one ---- pin1-1 and pin2-0  Right Motor Backwards
                    message.addArgument(1); //Right motor direction pin two
                    //message.addArgument(roll);
                    //Log.i("portError","Packet Created");

                    senderArduino.send(message);
                    //Log.i("portError","Packet Sent");
                    sleep(1);

                } catch (Exception e) {

                    //Log.i("portErrorArduino",e.toString());
                    // Error handling for some error
                }
            }

    }



    private void sendYawPitchRollPacketsToRemoteDevice() {

        //Log.i("portError","Remote Packet Called");

        if (senderRemote != null) {
            try {
                // Send the messages

                ///////Send Messages with arguments a multiple of 2------Very Important

                OSCMessage yawPitchRollMessage = new OSCMessage("/sensorValues");

                yawPitchRollMessage.addArgument(yawVal);
                yawPitchRollMessage.addArgument(pitchVal);
                yawPitchRollMessage.addArgument(rollVal);
                yawPitchRollMessage.addArgument(2);

                senderRemote.send(yawPitchRollMessage);
                //Log.i("portError","Packet Sent Remote");
                sleep(1);

            } catch (Exception e) {

                //Log.i("portError",e.toString());
                // Error handling for some error
            }
        }

    }





    @Override
    protected void onResume() {
        super.onResume();



        KpInput.setText(String.valueOf(Float.valueOf(file.getString("Kp","0"))));
        KiInput.setText(String.valueOf(Float.valueOf(file.getString("Ki","0"))));
        KdInput.setText(String.valueOf(Float.valueOf(file.getString("Kd","0"))));




        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);

        speakOut("Back. To. Home. Screen");

        /// OSC Initialization
        try {
            // Connect to some IP address and port
            senderArduino = new OSCPortOut(InetAddress.getByName(arduino_IP), ardunio_Port);
            senderRemote = new OSCPortOut(InetAddress.getByName(remoteDevice_IP), remoteDevice_Port);
            Log.i("portError","Sucess");
        } catch (Exception e) {
            // Error handling for any other errors
            Log.i("portError",e.getMessage());
        }




        ////Packet Sending Thread
        threadFlag=true;
        Thread sendPacketThread = new Thread() {
            @Override
            public void run() {

                while (threadFlag) {
                    sendPacketsToArduino();
                    sendYawPitchRollPacketsToRemoteDevice();
                }
            }
        };
        sendPacketThread.start();


        ///For receiving messages
        try {
            receiverRemote = new OSCPortIn(hotspot_Port);  ///Listening port number
            Log.i("portErrorReceiver","Sucess");

            OSCListener BotModeRemoteListener = new OSCListener() {
                public void acceptMessage(Date time, OSCMessage message) {

                    List<Object> args = message.getArguments();

                    //Log.i("ArraySize",message.getArguments().toString());

                    if(Objects.equals(String.valueOf(args.get(0)), "Manual..Bot..Mode......Entered"))
                    {
                        speakOut(String.valueOf(args.get(0)));
                        startActivity(new Intent(getApplicationContext(),ManualBotControllerMode.class));
                       // manualBotMode.callOnClick();
                    }
                    else if(Objects.equals(String.valueOf(args.get(0)),"Back. To. Home. Screen"))
                    {
                        speakOut(String.valueOf(args.get(0)));
                       // ManualBotControllerMode.fa.finish();
                    }



                    Log.i("portError",String.valueOf(args.get(0)));
                    ///received message
                }
            };
            receiverRemote.addListener("/BotMode", BotModeRemoteListener); //////Listening for Tag = '/test'


            OSCListener KpKiKdRemoteListener = new OSCListener() {
                public void acceptMessage(Date time, OSCMessage message) {

                    List<Object> args = message.getArguments();

                    //Log.i("ArraySize",message.getArguments().toString());
                    KpInput.setText(String.valueOf(args.get(0)));
                    KiInput.setText(String.valueOf(args.get(1)));
                    KdInput.setText(String.valueOf(args.get(2)));
                    ///received message
                }
            };
            receiverRemote.addListener("/PIDValues", KpKiKdRemoteListener); //////Listening for Tag = '/test'
            receiverRemote.startListening();
        } catch (SocketException e) {
            Log.d("OSCSendInitalisation", e.getMessage());
        }




    }


    @Override
    protected void onPause() {
        super.onPause();

        ////Uncomment this if you want to stop the sensors when phone is locked/screen off


        threadFlag=false;
       /*
       mSensorManager.unregisterListener(this);

       receiverRemote.close();

       if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
*/

        SharedPreferences.Editor edit = file.edit();
        edit.putString("Kp", String.valueOf(KpInput.getText())).apply();
        edit.putString("Ki", String.valueOf(KiInput.getText())).apply();
        edit.putString("Kd", String.valueOf(KdInput.getText())).apply();



    }

}
