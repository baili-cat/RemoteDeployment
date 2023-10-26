/*
 * Created by baili on 2021/01/21.
 */
package com.baili.sharingPlatform.common.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author baili
 * @date 2021/01/21.
 */
public class TypeConvertUtils {

	/**
	 * Escapes the given string with single quotes, if the input string contains a double quote or any of the
	 * given {@code charsToEscape}. Any single quotes in the input string will be escaped by doubling.
	 *
	 * <p>Given that the escapeChar is (;)
	 *
	 * <p>Examples:
	 * <ul>
	 *     <li>A,B,C,D => A,B,C,D</li>
	 *     <li>A'B'C'D => 'A''B''C''D'</li>
	 *     <li>A;BCD => 'A;BCD'</li>
	 *     <li>AB"C"D => 'AB"C"D'</li>
	 *     <li>AB'"D:B => 'AB''"D:B'</li>
	 * </ul>
	 *
	 * @param string a string which needs to be escaped
	 * @param charsToEscape escape chars for the escape conditions
	 * @return escaped string by single quote
	 */
	public static String escapeWithSingleQuote(String string, String... charsToEscape) {
		boolean escape = Arrays.stream(charsToEscape).anyMatch(string::contains) || string.contains("\"") || string.contains("'");

		if (escape) {
			return "'" + string.replaceAll("'", "''") + "'";
		}
		return string;
	}

	public static String convertToString(Object o) {
		if (o.getClass() == String.class) {
			return (String)o;
		} else if (o.getClass() == Duration.class) {
			Duration duration = (Duration)o;
			return String.format("%d ns", duration.toNanos());
		} else if (o instanceof List) {
			return ((List<?>)o).stream().map(e -> escapeWithSingleQuote(convertToString(e), ";")).collect(Collectors.joining(";"));
		} else if (o instanceof Map) {
			return ((Map<?, ?>)o).entrySet().stream().map(e -> {
				String escapedKey = escapeWithSingleQuote(e.getKey().toString(), ":");
				String escapedValue = escapeWithSingleQuote(e.getValue().toString(), ":");

				return escapeWithSingleQuote(escapedKey + ":" + escapedValue, ",");
			}).collect(Collectors.joining(","));
		}
		return o.toString();
	}

	public static Integer convertToInt(Object o) {
		if (o.getClass() == Integer.class) {
			return (Integer)o;
		} else if (o.getClass() == Long.class) {
			long value = (Long)o;
			if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
				return (int)value;
			} else {
				throw new IllegalArgumentException(String.format("Value %s overflows/underflows the integer type.", value));
			}
		}
		return Integer.parseInt(o.toString());
	}

	public static Long convertToLong(Object o) {
		if (o.getClass() == Long.class) {
			return (Long)o;
		} else if (o.getClass() == Integer.class) {
			return ((Integer)o).longValue();
		}
		return Long.parseLong(o.toString());
	}

	public static Boolean convertToBoolean(Object o) {
		if (o.getClass() == Boolean.class) {
			return (Boolean)o;
		}

		switch (o.toString().toUpperCase()) {
			case "TRUE":
				return true;
			case "FALSE":
				return false;
			default:
				throw new IllegalArgumentException(
						String.format("Unrecognized option for boolean: %s. Expected either true or false(case insensitive)", o));
		}
	}

	public static Float convertToFloat(Object o) {
		if (o.getClass() == Float.class) {
			return (Float)o;
		} else if (o.getClass() == Double.class) {
			double value = ((Double)o);
			if (value == 0.0 || (value >= Float.MIN_VALUE && value <= Float.MAX_VALUE) || (value >= -Float.MAX_VALUE && value <= -Float.MIN_VALUE)) {
				return (float)value;
			} else {
				throw new IllegalArgumentException(String.format("Value %s overflows/underflows the float type.", value));
			}
		}
		return Float.parseFloat(o.toString());
	}

	public static Double convertToDouble(Object o) {
		if (o.getClass() == Double.class) {
			return (Double)o;
		} else if (o.getClass() == Float.class) {
			return ((Float)o).doubleValue();
		}
		return Double.parseDouble(o.toString());
	}

}
