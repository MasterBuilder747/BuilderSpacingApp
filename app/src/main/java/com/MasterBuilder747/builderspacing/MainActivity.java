package com.MasterBuilder747.builderspacing;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private TextView buildText;
    private int build;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildText = findViewById(R.id.buildText);

//        NumberPicker buildN = findViewById(R.id.result3);
//        buildN.setMinValue(0);
//        buildN.setMaxValue(23);
//        buildN.setOnValueChangedListener(this);

        Button button = findViewById(R.id.calculate);
        button.setOnClickListener(v -> {


            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmm");
            Date date = new Date(System.currentTimeMillis());
            String currentTime = formatter.format(date);
            int month = Integer.parseInt(currentTime.substring(0, 2));
            int day = Integer.parseInt(currentTime.substring(2, 4));
            int year = Integer.parseInt(currentTime.substring(4, 8));
            int hour = Integer.parseInt(currentTime.substring(8, 10));

            //Toast.makeText(this, month + "\n" + day + "\n" + year + "\n" + hour, Toast.LENGTH_SHORT).show();

            //get user input here
            String build = "2d";
            String build1 = "1d";
            String build2 = "3d";

            //conversions and checks
            //decimal is in days
            double b = timeToDouble(build);
            double b1 = timeToDouble(build1);
            double b2 = timeToDouble(build2);
            if (b < 0 || b1 < 0 || b2 < 0) {
                //invalid
                //throw new IllegalArgumentException("Invalid build input.");
            }
            if (b2 < b1 || b2 == b1) {
                //throw new IllegalArgumentException("First build time must be greater than second build time.");
            }
            //limit the max build time to 30 days as Coc build times are never that high
            //and would break the calendar system if so
            if (b > 28 || b1 > 28 || b2 > 28) {
                //throw new IllegalArgumentException("Build time is too long (Max: 28).");
            }

            //formula: timeNeededToWaitToStartDesiredBuild =
            //build1 - [build{converted} - {builder spacing: }[(build2{converted} - build1{converted}) / 2]]
            if (b > b2) {
                //throw new IllegalArgumentException("The build time of the desired build is longer than the second build.");
            }
            //the spacing between b1 and b,
            //which is equal to the space between b and b2 as well
            double s = (b2 - b1) / 2;
            double t = b1 - (b - s);

            //convert back and output spacing and time to wait
            int[] space = doubleToTime(s);
            int[] wait = doubleToTime(t);
            if (space != null && wait != null) {

                //display the builder spacing of b in between b1 and b2
                if (space.length == 2) {
                    Toast.makeText(this, "Builder spacing: " + space[0] + " days, " + space[1] + " hours", Toast.LENGTH_LONG).show();
                } else if (space.length == 1) {
                    Toast.makeText(this, "Builder spacing: " + space[0] + " hours", Toast.LENGTH_LONG).show();
                }

                //find the time needed to wait until the build can be started
                if (wait.length == 2) {
                    day += wait[0];
                    hour += wait[1];
                    Toast.makeText(this, "Time needed to wait until you can start this build: " + wait[0] + " days, " + wait[1] + " hours", Toast.LENGTH_LONG).show();
                } else if (wait.length == 1) {
                    hour += wait[0];
                    Toast.makeText(this, "Time needed to wait until you can start this build: " + wait[0] + " hours", Toast.LENGTH_LONG).show();
                }

                //find the time, day, month, and year to start the build
                //change values accordingly
                //31 days: 1, 3, 5, 7, 8, 10, 12
                //30 days: 4, 6, 9, 11
                //28 or 29 days: 2
                if (hour > 23) {
                    hour -= 24;
                    day++;
                }
                if (month == 4 || month == 6 || month == 9 || month == 11) {
                    //30
                    if (day > 30) {
                        day -= 30;
                        month++;
                    }
                } else if (month == 2) {
                    if (year % 4 == 0) {
                        //29
                        if (day > 29) {
                            day -= 29;
                            month++;
                        }
                    } else {
                        //28
                        if (day > 28) {
                            day -= 28;
                            month++;
                        }
                    }
                } else {
                    //31
                    if (day > 31) {
                        day -= 31;
                        month++;
                    }
                }
                if (month > 12) {
                    month = 1;
                    year++;
                }

                //am/pm conversion
                if (hour > 12) {
                    //pm
                    hour -= 12;
                    Toast.makeText(this, "You can start the build on " + month + "-" + day + "-" + year + " at approximately " + hour + "pm", Toast.LENGTH_LONG).show();
                } else {
                    //am
                    Toast.makeText(this, "You can start the build on " + month + "-" + day + "-" + year + " at approximately " + hour + "am", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //buildText.setText(String.valueOf(oldVal)); //fixes the crash
    }

    //converts a string of days/hours to a double
    //format: DDdHHh or DdHh
    public static double timeToDouble(String time2) {
        String time = time2.replace(" ", "");
        double out = -1;
        if (time.contains("d")) {
            out = Double.parseDouble(time.substring(0, time.indexOf("d")));
            if (time.contains("h")) {
                double hours = Double.parseDouble(time.substring(time.indexOf("d") + 1, time.indexOf("h")));
                //add hours to day value
                if (hours < 24) {
                    out += hours / 24;
                } else {
                    //throw new IllegalArgumentException("Hours must be less than 24.");
                }
            }
        } else if (time.contains("h")) {
            //hours only
            out = Double.parseDouble(time.substring(0, time.indexOf("h"))) / 24;
        }
        return out;
    }
    public static int[] doubleToTime(double d) {
        int[] out = null;
        if (d > 1) {
            //days, hours
            out = new int[2];
            out[0] = (int)d;
            out[1] = (int)Math.round(24 * (d - out[0]));
        } else if (d == 1) {
            out = new int[2];
            out[0] = (int)d;
        } else if (d < 1 && d > 0) {
            //hours
            out = new int[1];
            out[0] = (int)Math.round(24 * d);
        }
        return out;
    }
}