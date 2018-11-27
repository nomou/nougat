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

    public DateTime(final long timestamp) {
        super(timestamp);
    }

    @Override
    public boolean before(final Date when) {
        return super.before(when);
    }

    @Override
    public boolean after(final Date when) {
        return super.after(when);
    }

    public boolean between(final Date start, final Date end) {
        final long timestamp = getTime();
        return timestamp >= start.getTime() && timestamp <= end.getTime();
    }

    public DateTime before(final long millis) {
        return before(millis, TimeUnit.MILLISECONDS);
    }

    public DateTime before(final long duration, final TimeUnit unit) {
        return new DateTime(getTime() - unit.toMillis(duration));
    }

    public DateTime after(final long millis) {
        return after(millis, TimeUnit.MILLISECONDS);
    }

    public DateTime after(final long duration, final TimeUnit unit) {
        return new DateTime(getTime() + unit.toMillis(duration));
    }

    /**
     * date time                  time unit     result
     * 1990-01-01 23:30:50.888    SECONDS       1990-01-01 23:30:50.000
     * 1990-01-01 23:30:50.888    MINUTES       1990-01-01 23:30:00.000
     * 1990-01-01 23:30:50.888    HOURS         1991-01-01 23:00:00.000
     * 1990-01-01 23:30:50.888    DAYS          1990-01-01 00:00:00.000
     *
     * @param precision the time precision
     * @return
     */
    public DateTime getTimeIn(final TimeUnit precision) {
        return getTimeIn(precision, TimeZone.getDefault());
    }

    public DateTime getTimeIn(final TimeUnit unit, final TimeZone zone) {
        final TimeUnit mills = TimeUnit.MILLISECONDS;
        final long timestamp = mills.convert(unit.convert(getTime(), mills), unit);
        return new DateTime(!TimeUnit.DAYS.equals(unit) ? timestamp : timestamp - zone.getOffset(timestamp));
    }

    public long elapsed(final Date datetime, final TimeUnit unit) {
        return elapsed(datetime.getTime(), unit);
    }

    public long elapsed(final long timestamp, final TimeUnit unit) {
        return unit.convert(getTime() - timestamp, TimeUnit.MILLISECONDS);
    }

    public Calendar toCalendar() {
        return toCalendar(TimeZone.getDefault());
    }

    public Calendar toCalendar(final TimeZone zone) {
        return toCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Calendar toCalendar(final TimeZone zone, final Locale locale) {
        Calendar cal = Calendar.getInstance(zone, locale);
        cal.setTimeInMillis(getTime());
        return cal;
    }

    @Override
    public String toString() {
        return toString("yyyy-MM-dd HH:mm:ss.SSS z");
    }

    public String toString(final String pattern) {
        return toString(pattern, TimeZone.getDefault());
    }

    public String toString(final String pattern, final TimeZone zone) {
        return toString(pattern, zone, null);
    }

    public String toString(final String pattern, final TimeZone zone, final Locale locale) {
        final DateFormat fmt = null != locale ? new SimpleDateFormat(pattern, locale) : new SimpleDateFormat(pattern);
        fmt.setTimeZone(zone);
        return toString(fmt);
    }

    public String toString(final DateFormat format) {
        return format.format(this);
    }

    public static DateTime now() {
        return new DateTime(System.currentTimeMillis());
    }

    public static DateTime wrap(final Date datetime) {
        return new DateTime(datetime.getTime());
    }

    public static DateTime wrap(final Calendar calendar) {
        return new DateTime(calendar.getTimeInMillis());
    }

    public static DateTime get(final String datetime, final DateFormat fmt) {
        try {
            return wrap(fmt.parse(datetime));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("fmt can not parse date string: " + datetime);
        }
    }

    public static DateTime get(final String datetime, final String pattern) {
        try {
            return wrap(new SimpleDateFormat(pattern).parse(datetime));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("date string '" + datetime + "' can not matches pattern: '" + pattern + '\'');
        }
    }
}
