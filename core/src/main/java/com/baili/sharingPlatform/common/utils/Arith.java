/*
 * Created by baili on 2019/05/28.
 */
package com.baili.sharingPlatform.common.utils;

import java.math.BigDecimal;

/**
 * 用于BigDecimal计算工具类
 *
 * @author baili
 * @date 2019/05/28.
 */
public final class Arith {

	/**
	 * 提供精确的加法运算。
	 *
	 * @param v1 被加数
	 * @param v2 加数
	 * @return 两个参数的和
	 */
	public static <T extends Number> BigDecimal add(T v1, T v2) {
		return convert(v1).add(convert(v2));
	}

	/**
	 * 提供精确的减法运算。
	 *
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 两个参数的差
	 */
	public static <T extends Number> BigDecimal subtract(T v1, T v2) {
		return convert(v1).subtract(convert(v2));
	}

	/**
	 * 提供精确的乘法运算。
	 *
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return 两个参数的积
	 */
	public static <T extends Number> BigDecimal multiply(T v1, T v2) {
		return convert(v1).multiply(convert(v2));
	}

	/**
	 * 提供精确的除法运算。
	 *
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 两个参数的商
	 */
	public static <T extends Number> BigDecimal divide(T v1, T v2) {
		return convert(v1).divide(convert(v2));
	}

	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
	 *
	 * @param v1 被除数
	 * @param v2 除数
	 * @param scale 表示表示需要精确到小数点以后几位。
	 * @return 两个参数的商
	 */
	public static <T extends Number> BigDecimal divide(T v1, T v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}

		return convert(v1).divide(convert(v2), scale, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 *
	 * @param v 需要四舍五入的数字
	 * @param scale 小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static <T extends Number> BigDecimal round(T v, int scale) {

		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		return convert(v).divide(convert(1), scale, BigDecimal.ROUND_HALF_UP);
	}

	private static <T extends Number> BigDecimal convert(T v) {
		if (v instanceof BigDecimal) {
			return (BigDecimal)v;
		}
		return new BigDecimal(String.valueOf(v));
	}

	/**
	 * 提供小数位截取处理。
	 *
	 * @param v 需要截取的数字
	 * @param scale 小数点后保留几位
	 * @return 截取的结果
	 */
	public static BigDecimal scale(BigDecimal v, int scale) {
		String plain = v.toPlainString();
		int index = plain.indexOf(".");
		if (index != -1 && plain.substring(index).length() >= (scale + 1)) {
			plain = plain.substring(0, index) + plain.substring(index, index + (scale + 1));
		}
		return new BigDecimal(plain);
	}

	/**
	 * =
	 */
	public static boolean equalTo(BigDecimal v1, BigDecimal v2) {
		return v1.compareTo(v2) == 0;
	}

	/**
	 * >
	 */
	public static boolean greaterThan(BigDecimal v1, BigDecimal v2) {
		return v1.compareTo(v2) > 0;
	}

	/**
	 * >=
	 */
	public static boolean greaterThanOrEqualTo(BigDecimal v1, BigDecimal v2) {
		return v1.compareTo(v2) >= 0;
	}

	/**
	 * <
	 */
	public static boolean lessThan(BigDecimal v1, BigDecimal v2) {
		return v1.compareTo(v2) < 0;
	}

	/**
	 * <=
	 */
	public static boolean lessThanOrEqualTo(BigDecimal v1, BigDecimal v2) {
		return v1.compareTo(v2) <= 0;
	}

}
