/*
 * Created by baili on 2020/11/14.
 */
package com.baili.sharingPlatform.common.utils;

import java.math.BigDecimal;

/**
 * @author baili
 * @date 2020/11/14.
 */
public final class SizeUtils {

	private static final BigDecimal BYTES = new BigDecimal(1);
	private static final BigDecimal KB    = new BigDecimal(BYTES.longValue() * 1024);
	private static final BigDecimal MB    = new BigDecimal(KB.longValue() * 1024);
	private static final BigDecimal GB    = new BigDecimal(MB.longValue() * 1024);
	private static final BigDecimal TB    = new BigDecimal(GB.longValue() * 1024);
	private static final BigDecimal PB    = new BigDecimal(TB.longValue() * 1024);

	private SizeUtils() {
	}

	public static String convert(Long value) {
		if (value == null) {
			return null;
		}

		BigDecimal size = new BigDecimal(value);
		if (size.longValue() >= PB.longValue()) {
			return divide(size, PB).toPlainString() + "P";
		} else if (size.longValue() >= TB.longValue()) {
			return divide(size, TB).toPlainString() + "T";
		} else if (size.longValue() >= GB.longValue()) {
			return divide(size, GB).toPlainString() + "G";
		} else if (size.longValue() >= MB.longValue()) {
			return divide(size, MB).toPlainString() + "M";
		} else if (size.longValue() >= KB.longValue()) {
			return divide(size, KB).toPlainString() + "K";
		} else {
			return size.toPlainString() + "Bytes";
		}
	}

	private static BigDecimal divide(BigDecimal v1, BigDecimal v2) {
		return v1.divide(v2, 1, BigDecimal.ROUND_DOWN).stripTrailingZeros();
	}

}
