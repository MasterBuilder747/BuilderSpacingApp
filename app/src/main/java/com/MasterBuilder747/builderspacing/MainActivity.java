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

    /*
    not working atm:
    1, 1d12h, 2

     */

    private NumberPicker buildHours;
    private NumberPicker build1Hours;
    private NumberPicker build2Hours;
    private NumberPicker buildDays;
    private NumberPicker build1Days;
    private NumberPicker build2Days;

    private int hours;
    private int hours1;
    private int hours2;
    private int days;
    private int days1;
    private int days2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //spinners
        //hours
        build1Days = findViewById(R.id.build1Days);
        build1Days.setMinValue(0);
        build1Days.setMaxValue(28);
        build1Days.setOnValueChangedListener(this);
        build2Days = findViewById(R.id.build2Days);
        build2Days.setMinValue(0);
        build2Days.setMaxValue(28);
        build2Days.setOnValueChangedListener(this);
        buildDays = findViewById(R.id.buildDays);
        buildDays.setMinValue(0);
        buildDays.setMaxValue(28);
        buildDays.setOnValueChangedListener(this);
        //days
        build1Hours = findViewById(R.id.build1Hours);
        build1Hours.setMinValue(0);
        build1Hours.setMaxValue(23);
        build1Hours.setOnValueChangedListener(this);
        build2Hours = findViewById(R.id.build2Hours);
        build2Hours.setMinValue(0);
        build2Hours.setMaxValue(23);
        build2Hours.setOnValueChangedListener(this);
        buildHours = findViewById(R.id.buildHours);
        buildHours.setMinValue(0);
        buildHours.setMaxValue(23);
        buildHours.setOnValueChangedListener(this);

        TextView result = findViewById(R.id.result);

        Button button = findViewById(R.id.calculate);
        button.setOnClickListener(v -> {
            //conversions and checks
            //decimal is in days
            double b = timeToDouble(days + "d" + hours + "h");
            double b1 = timeToDouble(days1 + "d" + hours1 + "h");
            double b2 = timeToDouble(days2 + "d" + hours2 + "h");
            boolean error = false;
            if ((days == 0 && hours == 0) || (days1 == 0 && hours1 == 0) || (days2 == 0 && hours2 == 0)) {
                Toast.makeText(this,"Values cannot be 0.", Toast.LENGTH_LONG).show();
                error = true;
            }
            if ((b2 < b1 || b2 == b1) && !error) {
                Toast.makeText(this,"First build time must be smaller than the second build time.", Toast.LENGTH_LONG).show();
                error = true;
            }
            //formula: timeNeededToWaitToStartDesiredBuild =
            //build1 - [build{converted} - {builder spacing: }[(build2{converted} - build1{converted}) / 2]]
            if (b > b2 && !error) {
                Toast.makeText(this,"The desired build time is longer than the second build time.", Toast.LENGTH_LONG).show();
                error = true;
            }
            double s = (b2 - b1) / 2;
            double t = b1 - (b - s);
            //convert back and output spacing and time to wait
            int[] space = doubleToTime(s);
            int[] wait = doubleToTime(t);

            if (t < 0 && !error) {
                if (wait.length == 1) {
                    Toast.makeText(this,"You missed the perfect timing for this build by " + Math.abs(wait[0]) + " hours!", Toast.LENGTH_LONG).show();
                }
                if (wait.length == 2) {
                    Toast.makeText(this,"You missed the perfect timing for this build by " + Math.abs(wait[0]) + " days and " + Math.abs(wait[1]) + " hours!", Toast.LENGTH_LONG).show();
                }
                error = true;
            }

            StringBuilder sb = new StringBuilder();
            if (wait.length == 0 && !error) {
                Toast.makeText(this,"You can start this build now!", Toast.LENGTH_LONG).show();
                if (space.length == 2) {
                    sb.append("Builder spacing: ").append(space[0]).append(" days, ").append(space[1]).append(" hours");
                } else if (space.length == 1) {
                    sb.append("Builder spacing: ").append(space[0]).append(" hours");
                }
                result.setText(sb.toString());
                error = true;
            }
            if (t > (b - 0.00001) && !error) {
                //TODO: handle this later
                sb.append("\nNOTE: The first build will end by the time you start this build. The spacing might be incorrect.");
            }

            //the spacing between b1 and b,
            //which is equal to the space between b and b2 as well
            if (!error) {
                //get current time/date information
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmm");
                Date date = new Date(System.currentTimeMillis());
                String currentTime = formatter.format(date);
                int month = Integer.parseInt(currentTime.substring(0, 2));
                int day = Integer.parseInt(currentTime.substring(2, 4));
                int year = Integer.parseInt(currentTime.substring(4, 8));
                int hour = Integer.parseInt(currentTime.substring(8, 10));
                //Toast.makeText(this, month + "\n" + day + "\n" + year + "\n" + hour, Toast.LENGTH_SHORT).show();

                //find the time needed to wait until the build can be started
                if (wait.length == 2) {
                    day += wait[0];
                    hour += wait[1];
                    sb.append("Time needed to wait until you can start this build: ").append(wait[0]).append(" days, ").append(wait[1]).append(" hours");
                } else if (wait.length == 1) {
                    hour += wait[0];
                    sb.append("Time needed to wait until you can start this build: ").append(wait[0]).append(" hours");
                }
                sb.append("\n");

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
                //am/pm conversion... yeah it starts at 12, not 1.... wtf is wrong with america...
                if (hour == 12) {
                    sb.append("You can start the build on ").append(month).append("-").append(day).append("-").append(year).append(" at approximately 12 pm.");
                } else if (hour == 0) {
                    sb.append("You can start the build on ").append(month).append("-").append(day).append("-").append(year).append(" at approximately 12 am.");
                } else if (hour > 12) {
                    //pm
                    hour -= 12;
                    sb.append("You can start the build on ").append(month).append("-").append(day).append("-").append(year).append(" at approximately ").append(hour).append(" pm.");
                } else {
                    //am
                    sb.append("You can start the build on ").append(month).append("-").append(day).append("-").append(year).append(" at approximately ").append(hour).append(" am.");
                }
                sb.append("\n");

                //display the builder spacing of b in between b1 and b2
                if (space.length == 2) {
                    sb.append("Builder spacing: ").append(space[0]).append(" days, ").append(space[1]).append(" hours");
                } else if (space.length == 1) {
                    sb.append("Builder spacing: ").append(space[0]).append(" hours");
                }

                result.setText(sb.toString());
            } else {
                result.setText("");
            }
        });
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (picker.getId() == R.id.buildDays) {
            days = newVal;
        }
        if (picker.getId() == R.id.build1Days) {
            days1 = newVal;
        }
        if (picker.getId() == R.id.build2Days) {
            days2 = newVal;
        }
        if (picker.getId() == R.id.buildHours) {
            hours = newVal;
        }
        if (picker.getId() == R.id.build1Hours) {
            hours1 = newVal;
        }
        if (picker.getId() == R.id.build2Hours) {
            hours2 = newVal;
        }
    }

    //converts a string of days/hours to a double
    //format: DDdHHh or DdHh
    public static double timeToDouble(String time2) {
        String time = time2.replace(" ", "");
        double out;
        if (time.contains("d")) {
            out = Double.parseDouble(time.substring(0, time.indexOf("d")));
            if (time.contains("h")) {
                double hours = Double.parseDouble(time.substring(time.indexOf("d") + 1, time.indexOf("h")));
                //add hours to day value
                out += (hours / 24);
            }
        } else {
            //hours only
            out = Double.parseDouble(time.substring(0, time.indexOf("h"))) / 24;
        }
        return out;
    }
    public static int[] doubleToTime(double d) {
        int[] out;
        if (d > -0.00001 && d < 0.00001) {
            //d = 0
            out = new int[0];
        } else if (d > 1 || d < -1) {
            //days, hours (includes negative values)
            out = new int[2];
            out[0] = (int)d;
            out[1] = (int)Math.round(24 * (d - out[0]));
        } else if (d-(int)d < 0.00001 && (d > 0.99999 || d < -0.99999)) {
            //hours = 0, works with negative
            out = new int[2];
            out[0] = (int)d;
        } else if (d < 1 && d > 0) {
            //hours only
            out = new int[1];
            out[0] = (int)Math.round(24 * d);
        } else {
            //hours only, negative
            out = new int[1];
            out[0] = (int)Math.round(24 * d);
        }
        return out;
    }
    public void display() {
        Toast.makeText(this, "build: " + days + " days, " + hours + " hours\n"
                        + "build1: " + days1 + " days, " + hours1 + " hours\n"
                        + "build2: " + days2 + " days, " + hours2 + " hours",
                Toast.LENGTH_SHORT).show();
    }
}