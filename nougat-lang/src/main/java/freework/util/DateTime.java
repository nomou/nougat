package freework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Date time extends java.util.Date.
 *
 * @author vacoor
 */
public class DateTime extends Date {

    /**
     * Creates a DateTime instance.
     *
     * @param timestamp the milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public DateTime(final long timestamp) {
        super(timestamp);
    }

    /**
     * Returns true if this date is before the specified date(not contains equals).
     *
     * @param when a date
     * @return true if this date is before the specified date
     */
    @Override
    public boolean before(final Date when) {
        return super.before(when);
    }

    /**
     * Returns true if this date is after the specified date(not contains equals).
     *
     * @param when a date
     * @return true if this date is after the specified date
     */
    @Override
    public boolean after(final Date when) {
        return super.after(when);
    }

    /**
     * Returns true if this date is between {@code start} and {@code end} (not contains equals).
     *
     * @param start the start date
     * @param end   the end date
     * @return true if this date is between {@code start} and {@code end}
     */
    public boolean between(final Date start, final Date end) {
        final long timestamp = getTime();
        return timestamp > start.getTime() && timestamp < end.getTime();
    }

    /**
     * Returns the date time after the this date time has moved backwards by a given milliseconds.
     *
     * @param millis the milliseconds of moving backwards
     * @return the date time of moved backwards
     */
    public DateTime backward(final long millis) {
        return backward(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the date time after the this date time has moved backwards by a given offset and unit.
     *
     * @param offset the offset of moving backwards
     * @param unit   the time unit
     * @return the date time of moved backwards
     */
    public DateTime backward(final long offset, final TimeUnit unit) {
        return new DateTime(getTime() - unit.toMillis(offset));
    }

    /**
     * Returns the date time after the this date time has moved forwards by a given milliseconds.
     *
     * @param millis the milliseconds of moving forwards
     * @return the date time of moved backwards
     */
    public DateTime forward(final long millis) {
        return forward(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the date time after the this date time has moved forwards by a given offset and unit.
     *
     * @param offset the offset of moving forwards
     * @param unit   the time unit
     * @return the date time of moved backwards
     */
    public DateTime forward(final long offset, final TimeUnit unit) {
        return new DateTime(getTime() + unit.toMillis(offset));
    }

    /**
     * Returns a given precision DateTime instance.
     * <p>
     * date time                  time unit     result
     * 1990-01-01 23:30:50.888    SECONDS       1990-01-01 23:30:50.000
     * 1990-01-01 23:30:50.888    MINUTES       1990-01-01 23:30:00.000
     * 1990-01-01 23:30:50.888    HOURS         1991-01-01 23:00:00.000
     * 1990-01-01 23:30:50.888    DAYS          1990-01-01 00:00:00.000
     *
     * @param precision the time precision
     * @return the date time for given precision
     */
    public DateTime getTimeIn(final TimeUnit precision) {
        return getTimeIn(precision, TimeZone.getDefault());
    }

    /**
     * Returns a given precision DateTime instance.
     *
     * @param unit the time unit
     * @param zone the time zone, used for computed at 0 o'clock every day
     * @return the date time for given precision
     */
    public DateTime getTimeIn(final TimeUnit unit, final TimeZone zone) {
        final TimeUnit mills = TimeUnit.MILLISECONDS;
        final long timestamp = mills.convert(unit.convert(getTime(), mills), unit);
        return new DateTime(!TimeUnit.DAYS.equals(unit) ? timestamp : timestamp - zone.getOffset(timestamp));
    }

    /**
     * Returns the number of the time unit that elapsed from the this datetime to the given datetime.
     *
     * @param datetime the datetime
     * @param unit     the time unit
     * @return the number of time unit, a negative number if the datetime is a past time
     */
    public long elapsed(final Date datetime, final TimeUnit unit) {
        return elapsed(datetime.getTime(), unit);
    }

    /**
     * Returns the number of the time unit that elapsed from the this datetime to the given timestamp.
     *
     * @param timestamp the datetime
     * @param unit      the time unit
     * @return the number of time unit, a negative number if the datetime is a past time
     */
    public long elapsed(final long timestamp, final TimeUnit unit) {
        return unit.convert(getTime() - timestamp, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a java.util.Date representing the this datetime.
     *
     * @return the date
     */
    public Date toDate() {
        return new Date(getTime());
    }

    /**
     * Returns a Calendar representing the this datetime.
     *
     * @return the calendar
     */
    public Calendar toCalendar() {
        return toCalendar(TimeZone.getDefault());
    }

    /**
     * Returns a Calendar representing the this datetime.
     *
     * @param zone the timezone
     * @return the calendar
     */
    public Calendar toCalendar(final TimeZone zone) {
        return toCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Returns a Calendar representing the this datetime.
     *
     * @param zone the timezone
     * @return the calendar
     */
    public Calendar toCalendar(final TimeZone zone, final Locale locale) {
        Calendar cal = Calendar.getInstance(zone, locale);
        cal.setTimeInMillis(getTime());
        return cal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString("yyyy-MM-dd HH:mm:ss.SSS z");
    }

    /**
     * Formats this DateTime into a date/time string.
     *
     * @param pattern the pattern of date/time
     * @return the formatted time string.
     */
    public String toString(final String pattern) {
        return toString(pattern, TimeZone.getDefault());
    }

    /**
     * Formats this DateTime into a date/time string.
     *
     * @param pattern the pattern of date/time
     * @param zone    the timezone
     * @return the formatted time string.
     */
    public String toString(final String pattern, final TimeZone zone) {
        return toString(pattern, zone, null);
    }

    /**
     * Formats this DateTime into a date/time string.
     *
     * @param pattern the pattern of date/time
     * @param zone    the timezone
     * @param locale  the locale
     * @return the formatted time string.
     */
    public String toString(final String pattern, final TimeZone zone, final Locale locale) {
        final DateFormat fmt = null != locale ? new SimpleDateFormat(pattern, locale) : new SimpleDateFormat(pattern);
        fmt.setTimeZone(zone);
        return toString(fmt);
    }

    /**
     * Formats this DateTime into a date/time string.
     *
     * @param format the datetime format
     * @return the formatted time string.
     */
    public String toString(final DateFormat format) {
        return format.format(this);
    }

    /* ************************************
     *           STATIC METHODS
     * ********************************** */

    /**
     * Returns a DateTime representing the current datetime.
     *
     * @return the datetime instance
     */
    public static DateTime now() {
        return new DateTime(System.currentTimeMillis());
    }

    /**
     * Returns a DateTime representing the specified date.
     *
     * @return the datetime instance
     */
    public static DateTime wrap(final Date datetime) {
        return new DateTime(datetime.getTime());
    }

    /**
     * Returns a DateTime representing the specified calendar.
     *
     * @return the datetime instance
     */
    public static DateTime wrap(final Calendar calendar) {
        return new DateTime(calendar.getTimeInMillis());
    }

    /**
     * Parses a given datetime string into the datetime object it represents.
     *
     * @param datetime the datetime string
     * @param fmt      the datetime string format
     * @return the datetime instance
     */
    public static DateTime get(final String datetime, final DateFormat fmt) {
        try {
            return wrap(fmt.parse(datetime));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("fmt can not parse date string: " + datetime);
        }
    }

    /**
     * Parses a given datetime string into the datetime object it represents.
     *
     * @param datetime the datetime string
     * @param pattern  the pattern of datetime string
     * @return the datetime instance
     */
    public static DateTime get(final String datetime, final String pattern) {
        try {
            return wrap(new SimpleDateFormat(pattern).parse(datetime));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("date string '" + datetime + "' can not matches pattern: '" + pattern + '\'');
        }
    }
}
