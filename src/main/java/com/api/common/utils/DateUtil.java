package com.api.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class DateUtil {
    private static SimpleDateFormat dataformat_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat dataformat_yyyy_MM_dd_HH_mm_ss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // #region version 2.0
    public static String getCurrentTimeStampWithNoT(String... dataformats) {
        SimpleDateFormat df = dataformats.length > 0 ? new SimpleDateFormat(dataformats[0]) : dataformat_yyyy_MM_dd_HH_mm_ss;
        return df.format(Calendar.getInstance().getTime());
    }

    public static boolean isPastDate(int date) { // yyyyMMdd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date pastDate = null;
        Date todayDate = null;

        // getTodayDate
        Calendar cal = Calendar.getInstance();
        cal.setTime(Calendar.getInstance().getTime());
        Date today = cal.getTime();
        String todayStr = dateFormat.format(today);

        try {
            pastDate = dateFormat.parse(String.valueOf(date));
            todayDate = dateFormat.parse(todayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return pastDate.before(todayDate);
    }

    public static String getCurrentTimeStamp() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()).replace(" ","T");
    }

    public static boolean isSameTimeForTimeString(String timeA, String timeB) {
        LocalDateTime dateTimeA = LocalDateTime.parse(timeA.replace(" ","T"));
        LocalDateTime dateTimeB = LocalDateTime.parse(timeB.replace(" ","T"));
        return dateTimeA.getDayOfYear() == dateTimeB.getDayOfYear() && dateTimeA.getHour() == dateTimeB.getHour()
                && dateTimeA.getMinute() == dateTimeB.getMinute();
    }

    public static boolean isSameDay(String dateStrA, String dateStrB) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date dateA = null;
        Date dateB = null;
        try {
            dateA = dateFormat.parse(dateStrA);
            dateB = dateFormat.parse(dateStrB);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateA.equals(dateB);
    }

    public static boolean isSameDay(Date dateA, Date dateB) {
        Calendar calendarA = Calendar.getInstance();
        calendarA.setTime(dateA);
        Calendar calendarB = Calendar.getInstance();
        calendarB.setTime(dateB);
        return calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR);
    }

    public static int getNextBusinessDay(Integer intFromDate, int daysDelay, List<Integer> intHolidays, boolean isForward) {
        Date fromDate = getDateFromString(intFromDate.toString());
        List<Date> holidays = new ArrayList<>();
        Iterator var6 = intHolidays.iterator();
        while(var6.hasNext()) {
            Integer holiday = (Integer)var6.next();
            holidays.add(getDateFromString(holiday.toString()));
        }
        return Integer.parseInt(getFormattedStringFromDate(getNextBusinessDay(fromDate, daysDelay, holidays, isForward)));
    }

    public static Date getNextBusinessDay(Date myDate, int daysDelay, List<Date> holidays, boolean isForward) {
        if (daysDelay == 0) {
            return myDate;
        } else {
            int direction = isForward ? 1 : -1;
            int counter = 0;
            do {
                for (myDate = addDays(myDate, direction); isHoliday(myDate, holidays); myDate = addDays(myDate, direction)) {}
                ++counter;
            } while (daysDelay > counter);
            return myDate;
        }
    }

    private static boolean isHoliday(Date d, List<Date> holidays) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return isWeekend(d) || holidays.stream().filter((it) -> {
            return dateFormat.format(it).equals(dateFormat.format(d));
        }).findFirst().isPresent();
    }

    public static Date getDateFromString(String stringDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            return dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFormattedStringFromDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    public static String getBusinessDateString(SimpleDateFormat dateFormat, int days, String currency, String drms_str) {
        Calendar cal_add = Calendar.getInstance();
        cal_add.add(Calendar.DATE, days); // number represents number of days
        Date pastDate = cal_add.getTime();
        if(days != 0) {
            int num = days > 0 ? days + 5 : -5 + days;
            while(isWeekend(pastDate)) {
                num = num + Math.abs(num);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, num);
                pastDate = cal.getTime();
            }
        }
        return dateFormat.format(pastDate);
    }

    public static String getBusinessDateStringForOneDay(SimpleDateFormat dateFormat, String dateInString, int days, String currency, String drms_str) {
        Date date = null;
        try {
            date = dateFormat.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal_add = Calendar.getInstance();
        cal_add.setTime(date);
        cal_add.add(Calendar.DATE, days); // number represents number of days
        Date pastDate = cal_add.getTime();
        if(days != 0) {
            while(isWeekend(pastDate)) {
                cal_add.add(Calendar.DATE, days/Math.abs(days));
                pastDate = cal_add.getTime();
            }
        }
        return dateFormat.format(pastDate);
    }

    public static String getDateString(SimpleDateFormat dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days); // number represents number of days
        Date pastDate = cal.getTime();
        return dateFormat.format(pastDate);
    }

    public static String getCurrentTimeLog() {
        return "[" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()) + "] ";
    }

    private static Date addDays(Date d, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static boolean isWeekend(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static String getCurrentDateStr() {
        return (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime());
    }

    public static String getDateStringByYearMonthDay(int year, int month, int day) {
        String yearStr = "" + year;
        String monthStr = month < 10 ? "0" + month : "" + month;
        String dayStr = day < 10 ? "0" + day : "" + day;
        return yearStr + monthStr + dayStr;
    }

    public static boolean isDateALargerThanDateB(String dateAStr, String dateBStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date dateA = null;
        Date dateB = null;
        try {
            dateA = dateFormat.parse(dateAStr);
            dateB = dateFormat.parse(dateBStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateB.before(dateA);
    }

    public static String getTargetDateWithFormat(int days, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Date todate1 = cal.getTime();
        String targetDate = simpleDateFormat.format(todate1);
        return targetDate;
    }

    public static boolean isDateTimeALargerThanDateB(String dateAStr, String dateBStr, String dateFormatString) { // yyyy-MM-dd HH:mm:ss.SSS
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        Date dateA = null;
        Date dateB = null;
        try {
            dateA = dateFormat.parse(dateAStr);
            dateB = dateFormat.parse(dateBStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateB.before(dateA);
    }
}