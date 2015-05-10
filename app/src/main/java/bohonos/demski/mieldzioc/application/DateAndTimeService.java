package bohonos.demski.mieldzioc.application;

import java.util.GregorianCalendar;

/**
 * Created by Dominik on 2015-05-02.
 */
public class DateAndTimeService {

    /**
     * Je�eli liczba jest jednocyfrowa dodaje wiod�ce zero przed ni�
     *
     * @param liczba
     * @return
     */
    public static String addFirstZeros(int liczba) {
        String zwrot = (liczba < 10 && liczba >= 0) ? "0" + liczba : String.valueOf(liczba);
        return zwrot;
    }

    public static String addFirstTwoZeros(int liczba) {
        if (liczba < 10 && liczba >= 0) {
            return "00" + liczba;
        } else if (liczba >= 10 && liczba < 100) {
            return "0" + liczba;
        } else return String.valueOf(liczba);
    }

    /**
     * @return zwraca obecn� dat� i godzin� w postaci:  "YYYY-MM-DD HH:MM:SS.SSS"
     */
    public static String getToday() {
        GregorianCalendar today = new GregorianCalendar();
        return today.get(GregorianCalendar.YEAR) + "-" +
                addFirstZeros(today.get(GregorianCalendar.MONTH) + 1) + "-" +
                addFirstZeros(today.get(GregorianCalendar.DATE))
                + " " + addFirstZeros(today.get(GregorianCalendar.HOUR_OF_DAY))
                + ":" + addFirstZeros(today.get(GregorianCalendar.MINUTE))
                + ":" + addFirstZeros(today.get(GregorianCalendar.SECOND)) + "." +
                addFirstTwoZeros(today.get(GregorianCalendar.MILLISECOND));
    }

    /**
     * @param date data do konwersji
     * @return zwraca zadan� dat� i godzin� w postaci:  "YYYY-MM-DD HH:MM:SS.SSS"
     */
    public static String getDateAsDBString(GregorianCalendar date) {
        return date.get(GregorianCalendar.YEAR) + "-" +
                addFirstZeros(date.get(GregorianCalendar.MONTH) + 1) + "-" +
                addFirstZeros(date.get(GregorianCalendar.DATE))
                + " " + addFirstZeros(date.get(GregorianCalendar.HOUR_OF_DAY))
                + ":" + addFirstZeros(date.get(GregorianCalendar.MINUTE))
                + ":" + addFirstZeros(date.get(GregorianCalendar.SECOND)) + "." +
                addFirstTwoZeros(date.get(GregorianCalendar.MILLISECOND));
    }

    /**
     * Zwraca obiekt GregorianCalendar z zadanego stringa.
     * @param date data w formacie "yyyy-mm-dd hh:mm:ss".
     * @return obiekt GregorianCalendar lub null, je�li podano b��dny format.
     */
    public static GregorianCalendar getDateFromString(String date) {
        try {
            int day = Integer.valueOf(date.substring(0, 2));
            int month = Integer.valueOf(date.substring(3, 5));
            int year = Integer.valueOf(date.substring(6, 10));
            int hour = Integer.valueOf(date.substring(11, 13));
            int minute = Integer.valueOf(date.substring(14, 16));
            int second = Integer.valueOf(date.substring(17));
            GregorianCalendar toReturn = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            return toReturn;
        } catch (Exception e) {
            return null;
        }
    }
}
