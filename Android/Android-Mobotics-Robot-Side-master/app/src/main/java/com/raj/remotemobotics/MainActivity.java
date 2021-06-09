package com.raj.remotemobotics;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    Button openCamButton,kpSend,kiSend,kdSend,manualBotMode;
    EditText KpInput,KiInput,KdInput;

    SharedPreferences file;  ///For storing Kp Ki Kd values after app shutdown



    private TextView yaw,pitch,roll,statusTextView;

    private String remoteDevice_IP="192.168.43.66";  ///Remote device Ip
    private int remoteDevice_Port= 6666;        ///Remote device Listening port

    private String hotspot_IP="192.168.43.1";  ///Remote device Ip
    private int hotspot_Port= 1111;        ///Remote device Listening port

    public static OSCPortOut sender;
    private OSCPortIn receiver;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ///For Hiding the Keyboard intially
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        openCamButton =  findViewById(R.id.openCamButton);
        manualBotMode=findViewById(R.id.manualBotMode);
        kpSend =  findViewById(R.id.kpSend);
        kiSend =  findViewById(R.id.kiSend);
        kdSend =  findViewById(R.id.kdSend);

        KpInput=  findViewById(R.id.KpInput);
        KiInput=  findViewById(R.id.KiInput);
        KdInput=  findViewById(R.id.KdInput);



        statusTextView=  findViewById(R.id.statusTextView);
        yaw=    findViewById(R.id.yaw);
        pitch=  findViewById(R.id.pitch);
        roll=   findViewById(R.id.roll);


        ///Retriving the values of Kp Ki Kd that were saved after the app was shutdown
        file = getSharedPreferences("save", 0);




        openCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),CameraSettingsActivity.class));

            }
        });


        manualBotMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPIDValuePacketsToHotSpotDevice("Manual..Bot..Mode........Entered","","",2);
                startActivity(new Intent(getApplicationContext(),ManualBotControllerMode.class));
            }
        });





    }


    ///Button Click Functions

    public void sendPIDValues(View view) {

        Toast.makeText(this,String.valueOf(view.getId()),Toast.LENGTH_SHORT).show();

        sendPIDValuePacketsToHotSpotDevice(String.valueOf(KpInput.getText()),String.valueOf(KiInput.getText()),String.valueOf(KdInput.getText()),1);
        /*Thread thread = new Thread() {
            @Override
            public void run() {
                sendPIDValuePacketsToHotSpotDevice();
            }
        };
        thread.start();
        Toast.makeText(this,"send",Toast.LENGTH_SHORT).show();
*/

    }
    ///-------------------------------------------------------------------------------------------




    private void sendPIDValuePacketsToHotSpotDevice(final String s1, final String s2, final String s3, final int mode) {

        final Thread thread=new Thread() {
            @Override
            public void run() {

                Log.i("portError","Packet Called");

                if (sender != null) {
                    try {

                        Log.i("portError","Packet in");

                        ///////Send Messages with arguments a multiple of 2------Very Important
                        if(mode==1) {
                            OSCMessage message = new OSCMessage("/PIDValues");
                            message.addArgument(Float.valueOf(s1));
                            message.addArgument(Float.valueOf(s2));
                            message.addArgument(Float.valueOf(s3));
                            message.addArgument(2);
                            sender.send(message);
                            //message.addArgument(roll);
                            //Log.i("portError","Packet Created");
                            Log.i("portError","PID Packet Sent");
                        }
                        else if(mode==2)
                        {
                            OSCMessage message = new OSCMessage("/BotMode");
                            message.addArgument(s1);
                            message.addArgument("");
                            sender.send(message);
                            Log.i("portError","Bot Mode Packet Sent");

                        }





                    } catch (Exception e) {

                        Log.i("portError",e.toString());
                        // Error handling for some error
                    }
                }


            }
        };

        thread.start();

    }




    @Override
    protected void onResume() {
        super.onResume();


        KpInput.setText(String.valueOf(Float.valueOf(file.getString("Kp","0"))));
        KiInput.setText(String.valueOf(Float.valueOf(file.getString("Ki","0"))));
        KdInput.setText(String.valueOf(Float.valueOf(file.getString("Kd","0"))));




        /// OSC Initialization
        ///For sending messages to the Hotspot
        try {
            // Connect to some IP address and port
            sender = new OSCPortOut(InetAddress.getByName(hotspot_IP), hotspot_Port);
            Log.i("portErrorSender","Sucess");

        } catch (Exception e) {
            // Error handling for any other errors
            Log.i("portError",e.getMessage());
        }

        sendPIDValuePacketsToHotSpotDevice("Back. To. Home. Screen","","",2);


        ///For receiving messages from Hotspot
        try {

             receiver = new OSCPortIn(remoteDevice_Port);  ///Listening port number


            ////PID Values Listener
            OSCListener pidListener = new OSCListener() {
                public void acceptMessage(Date time, OSCMessage message) {

                    List<Object> args = message.getArguments();

                    //Log.i("ArraySize",message.getArguments().toString());
                    KpInput.setText(String.valueOf(args.get(0)));
                    KiInput.setText(String.valueOf(args.get(1)));
                    KdInput.setText(String.valueOf(args.get(2)));

                    ///received message
                }
            };
            receiver.addListener("/KpKiKdValues", pidListener); //////Listening for Tag = '/test'



            ////Sensor Values Listener
            OSCListener listener = new OSCListener() {
                public void acceptMessage(Date time, OSCMessage message) {

                    List<Object> args = message.getArguments();

                    //Log.i("ArraySize",message.getArguments().toString());
                    yaw.setText(String.valueOf(args.get(0)));
                    pitch.setText(String.valueOf(args.get(1)));
                    roll.setText(String.valueOf(args.get(2)));

                    ///received message
                }
            };
            receiver.addListener("/sensorValues", listener); //////Listening for Tag = '/test'
            receiver.startListening();
        } catch (SocketException e) {
            Log.d("OSCSendInitalisation", e.getMessage());
        }



    }

    @Override
    protected void onPause() {
        super.onPause();

        //receiver.close();


        SharedPreferences.Editor edit = file.edit();
        edit.putString("Kp", String.valueOf(KpInput.getText())).apply();
        edit.putString("Ki", String.valueOf(KiInput.getText())).apply();
        edit.putString("Kd", String.valueOf(KdInput.getText())).apply();




    }






}


