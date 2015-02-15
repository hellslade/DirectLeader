package ru.tasu.directleader;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";
    
    public static final Locale mLocale = new Locale("ru","RU");;
    
    private static String reverseString(String s) {
        String newString = "";
        for (int i=0; i<s.length(); i++) {
            newString = s.charAt(i) + newString;
        }
        return newString;
    }

    /**
     * This method also assumes endDate >= startDate
    **/
    public static long daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);
        
        if (eDate.compareTo(sDate) == 0) {
            return 0;
        }
        
        if (endDate.before(startDate)) {
            return -1;
        }

        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }
    
    public static Calendar getDatePart(Date date){
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);      
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }
    public static String formatFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "ב", "ךב", "לב", "דב", "עב" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
