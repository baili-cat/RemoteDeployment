/*
 * Created by baili on 2020/06/09.
 */
package com.baili.sharingPlatform.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

/**
 * @author baili
 * @date 2020/06/09.
 */
public final class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final long Nanosecond  = 1;
	private static final long Microsecond = 1000 * Nanosecond;
	private static final long Millisecond = 1000 * Microsecond;
	private static final long Second      = 1000 * Millisecond;
	private static final long Minute      = 60 * Second;
	private static final long Hour        = 60 * Minute;

	private DateUtils() {
	}

	public static String toDurationString(long millis) {
		return toDurationString(Duration.ofMillis(millis));
	}

	public static String toDurationString(Duration duration) {
		if (duration.isZero()) {
			return "0s";
		}

		long nanos = Math.abs(duration.toNanos());
		String val;

		if (Math.abs(nanos) < Second) {
			if (nanos < Microsecond) {
				val = nanos + "ns";
			} else if (nanos < Millisecond) {
				val = nanos / Microsecond + "µs";
			} else {
				val = nanos / Millisecond + "ms";
			}
		} else {
			StringBuilder builder = new StringBuilder();

			long second = nanos / Second;
			if (second % 60 > 0) {
				builder.append(second % 60).append("s");
			}

			long minute = nanos / Minute;
			if (minute % 60 > 0) {
				builder.insert(0, minute % 60 + "m");
			}

			long hour = nanos / Hour;
			if (hour % 60 > 0) {
				builder.insert(0, hour % 60 + "h");
			}
			val = builder.toString();
		}
		return duration.isNegative() ? "-" + val : val;
	}

	public static Date toDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date toMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date lastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		// calendar.set(Calendar.DAY_OF_MONTH,1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date parse(String date, String pattern) throws ParseException {
		return new SimpleDateFormat(pattern).parse(date);
	}

	public static String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

}
