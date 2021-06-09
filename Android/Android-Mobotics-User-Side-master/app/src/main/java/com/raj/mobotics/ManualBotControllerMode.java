package com.raj.mobotics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;


public class ManualBotControllerMode extends AppCompatActivity {


    @SuppressLint("StaticFieldLeak")
    public static Activity fa;


    SharedPreferences file;

    //private Handler repeatUpdateHandler = new Handler();
    private OSCPortOut senderArduino;
    Boolean zeroPacket=false;
    int onClickTouchSensitivity =1,intialPWM=400, sleepTime =4;   ///For single Click , no. of packets to be sent/No.of times loop should run
    int leftPwmValue,rightPwmValue;

    SeekBar leftPwmSeekBar,rightPwmSeekBar;

    AtomicBoolean botForwardMotionDownFlag=new AtomicBoolean(false);
    AtomicBoolean botBackwardMotionDownFlag=new AtomicBoolean(false);
    AtomicBoolean botLeftMotionDownFlag=new AtomicBoolean(false);
    AtomicBoolean botRightMotionDownFlag=new AtomicBoolean(false);   ////MotionEvent.ActionDown , then it is true....used to check the action of the click

    private EditText leftPwmValueText,rightPwmValueText;

    Button forward,backward,stop,spotLeft,spotRight,left,right;

    private String arduino_IP = "192.168.43.55";  ///Static IP for Arduino
    private int ardunio_Port = 5555;              ///Port where Arduino is listening


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_bot_controller_mode);


        fa=this;

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        ///Retriving the values of leftPwm and RightPwm that were saved after the app was shutdown
        file = getSharedPreferences("save", 0);


        forward = findViewById(R.id.forward);
        backward=findViewById(R.id.backward);
        left=findViewById(R.id.left);
        right=findViewById(R.id.right);
        spotLeft=findViewById(R.id.spotLeft);
        spotRight=findViewById(R.id.spotRight);
        stop=findViewById(R.id.stop);

        ////Bot Button Clicks
        botControlButtonClickListeners();





        leftPwmSeekBar =findViewById(R.id.leftPwmSeekBar);
        leftPwmSeekBar.setMin(0);
        leftPwmSeekBar.setMax(1000);
        leftPwmSeekBar.setProgress(intialPWM);
        leftPwmValue =intialPWM;


        rightPwmSeekBar =findViewById(R.id.rightPwmSeekBar);
        rightPwmSeekBar.setMin(0);
        rightPwmSeekBar.setMax(1000);
        rightPwmSeekBar.setProgress(intialPWM);
        rightPwmValue =intialPWM;


        leftPwmValueText =findViewById(R.id.leftPwmValueText);
        leftPwmValueText.setText(String.valueOf(leftPwmValue));


        rightPwmValueText =findViewById(R.id.rightPwmValueText);
        rightPwmValueText.setText(String.valueOf(rightPwmValue));




        Button leftPwmSetButton=findViewById(R.id.leftPwmSetButton);
        Button rightPwmSetButton=findViewById(R.id.rightPwmSetButton);

        leftPwmSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftPwmSeekBar.setProgress(Integer.valueOf(String.valueOf(leftPwmValueText.getText())));

            }
        });

        rightPwmSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightPwmSeekBar.setProgress(Integer.valueOf(String.valueOf(rightPwmValueText.getText())));

            }
        });



        //////////////PWM SeekBar Listener Starts//////////////////////////


        leftPwmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                leftPwmValue = seekBar.getProgress();
                leftPwmValueText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rightPwmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                rightPwmValue = seekBar.getProgress();
                rightPwmValueText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        ////////////////PWM SeekBar Code Ends////////////////////////////////



    }

    ///////////////BOT Motion Functions Starts/////////////////////////////////////


    public void botForward() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,0,1,0,1);
            }
        };

         thread.start();

    }

    public void botBackward() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,1,0,1,0);
            }
        };

        thread.start();

    }

    public void botLeft() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,0,0,0,1);
            }
        };

        thread.start();

    }


    public void botRight() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,0,1,0,0);
            }
        };

        thread.start();

    }


    public void botSpotLeft() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,1,0,0,1);
            }
        };

        thread.start();

    }


    public void botSpotRight() {
        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(leftPwmValue,rightPwmValue,0,1,1,0);
            }
        };

        thread.start();

    }


    public void botStop() throws InterruptedException {

        sleep(sleepTime);

        Thread thread =new Thread(){

            @Override
            public void run() {
                sendPacketsToArduino(0,0,1,1,1,1);
            }
        };

        thread.start();



    }


    ///////////////BOT Motion Functions Ends/////////////////////////////////////






    ///Packet Sending Function

    private void sendPacketsToArduino(int pwmLeft,int pwmRight,int leftPin1,int leftPin2,int rightPin1,int rightPin2) {


        if (senderArduino != null) {
            try {

                ///////Send Messages with arguments a multiple of 2------Very Important
                Log.i("portError", "forward Entered");
                OSCMessage message = new OSCMessage("/motorValues");
                message.addArgument((float) pwmLeft); //Left Motor PWM Value
                message.addArgument((float) pwmRight); //Right Motor PWM Value

                message.addArgument(leftPin1); //Left motor direction pin one    ---- pin1-0 and pin2-1  (Left Motor Forwards)
                message.addArgument(leftPin2); //Left motor direction pin two

                message.addArgument(rightPin1); //Right motor direction pin one   ---- pin1-0 and pin2-1  (Right Motor Forwards)
                message.addArgument(rightPin2); //Right motor direction pin two

                senderArduino.send(message);

                Log.i("portError", message.getArguments().toString());
                //zeroPacket=true;
                //Log.i("portError","Packet Sent");
                //sleep(1);

            } catch (Exception e) {

                Log.i("portError", e.getMessage());
                //Log.i("portErrorArduino",e.toString());
                // Error handling for some error
            }
        }


    }



    @Override
    protected void onResume() {
        super.onResume();

        leftPwmSeekBar.setProgress(Integer.valueOf(file.getString("leftPwm","400")));
        rightPwmSeekBar.setProgress(Integer.valueOf(file.getString("rightPwm","400")));


        /// OSC Initialization
        try {
            Log.i("portError","Entered");
            // Connect to Arduino IP address and port
            senderArduino = new OSCPortOut(InetAddress.getByName(arduino_IP), ardunio_Port);
            Log.i("portError","Sucess");
        } catch (Exception e) {
            // Error handling for any other errors
            Log.i("portError",e.getMessage());
        }

    }


    @Override
    protected void onPause() {
        super.onPause();


        SharedPreferences.Editor edit = file.edit();
        edit.putString("leftPwm", String.valueOf(leftPwmValueText.getText())).apply();
        edit.putString("rightPwm", String.valueOf(rightPwmValueText.getText())).apply();



        Thread thread = new Thread() {

                @Override
                public void run() {
                    try {
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

            Thread thread = new Thread() {

                @Override
                public void run() {
                    try {
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

    }




    private void botControlButtonClickListeners() {

        ////////////////////CLICK LISTENERS CODE STARTS//////////////////////////


        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botForwardMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botForwardMotionDownFlag.set(true);
                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botForwardMotionDownFlag.get())
                            {
                                botForward();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                    motionListenerThread.start();
                }


                return false;
            }
        });



        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botBackwardMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botBackwardMotionDownFlag.set(true);

                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botBackwardMotionDownFlag.get())
                            {
                                botBackward();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    motionListenerThread.start();
                }

                return false;
            }
        });


        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botBackwardMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botBackwardMotionDownFlag.set(true);

                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botBackwardMotionDownFlag.get())
                            {
                                try {
                                    botStop();
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    motionListenerThread.start();
                }

                return false;
            }
        });



        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botLeftMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botLeftMotionDownFlag.set(true);
                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botLeftMotionDownFlag.get())
                            {
                                botLeft();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                    motionListenerThread.start();
                }


                return false;
            }
        });


        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botRightMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botRightMotionDownFlag.set(true);
                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botRightMotionDownFlag.get())
                            {
                                botRight();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                    motionListenerThread.start();
                }


                return false;
            }
        });



        spotLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botRightMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botRightMotionDownFlag.set(true);
                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botRightMotionDownFlag.get())
                            {
                                botSpotLeft();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                    motionListenerThread.start();
                }


                return false;
            }
        });




        spotRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    botRightMotionDownFlag.set(false);
                    try {
                        botStop();
                        botStop();
                        botStop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                else if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    botRightMotionDownFlag.set(true);
                    Thread motionListenerThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(botRightMotionDownFlag.get())
                            {
                                botSpotRight();

                                try {
                                    sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                    motionListenerThread.start();
                }


                return false;
            }
        });


        /////////////////////CLICK LISTENERS CODE ENDS ///////////////////////////////

    }










}
