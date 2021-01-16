package project.helper;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;

import java.util.ArrayList;

public class HelperString {

    ArrayList<String> months = new ArrayList<String>();

    public static String getTransformedTime(String time) {
        return time.substring(0, time.lastIndexOf(":"));
    }

    public static String getTransformedDate(String date) {

        String[] dateArray = date.split("-");
        HelperCalendar helperCalendar = new HelperCalendar();
        helperCalendar.gregorianToPersian(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]));
        int d = helperCalendar.getDay();
        String day = "";
        if (d < 10) {
            day += "0";
        }
        day += d;

        int m = helperCalendar.getMonth();
        String month = "";
        if (m < 10) {
            month += "0";
        }
        month += m;

        switch (m) {
            case 1:
                month = App.getContext().getString(R.string.month_1);
                break;
            case 2:
                month = App.getContext().getString(R.string.month_2);
                break;
            case 3:
                month = App.getContext().getString(R.string.month_3);
                break;
            case 4:
                month = App.getContext().getString(R.string.month_4);
                break;
            case 5:
                month = App.getContext().getString(R.string.month_5);
                break;
            case 6:
                month = App.getContext().getString(R.string.month_6);
                break;
            case 7:
                month = App.getContext().getString(R.string.month_7);
                break;
            case 8:
                month = App.getContext().getString(R.string.month_8);
                break;
            case 9:
                month = App.getContext().getString(R.string.month_9);
                break;
            case 10:
                month = App.getContext().getString(R.string.month_10);
                break;
            case 11:
                month = App.getContext().getString(R.string.month_11);
                break;
            case 12:
                month = App.getContext().getString(R.string.month_12);
                break;
        }


        //return (helperCalendar.getYear() + "/" + month + "/" + day);
        return (month + " "+helperCalendar.getYear());
    }
}
