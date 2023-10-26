/*
 * Created by baili on 2020/11/30.
 */
package com.baili.sharingPlatform.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author baili
 * @date 2020/11/30.
 */
public class VersionUtils {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+(.\\d+)*$");

	private static final Pattern INTERCEPT_VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");

	public static boolean checkVersion(String version) {
		return VERSION_PATTERN.matcher(version).matches();
	}

	public static String interceptVersion(String version){

		// 创建 Matcher 对象
		Matcher matcher = INTERCEPT_VERSION_PATTERN.matcher(version);

		// 查找并输出匹配的子串
		while (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static int compare(String thisVersion, String targetVersion) {
		String[] thisParts = thisVersion.split("\\.");
		String[] targetParts = targetVersion.split("\\.");
		int length = Math.max(thisParts.length, targetParts.length);

		for (int i = 0; i < length; ++i) {
			long thisPart = i < thisParts.length ? Long.parseLong(thisParts[i]) : 0;
			long thatPart = i < targetParts.length ? Long.parseLong(targetParts[i]) : 0;
			if (thisPart < thatPart) {
				return -1;
			}
			if (thisPart > thatPart) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * thisVersion 大于 targetVersion
	 */
	public static boolean greaterThan(String thisVersion, String targetVersion) {
		return compare(thisVersion, targetVersion) > 0;
	}

	/**
	 * thisVersion 大于等于 targetVersion
	 */
	public static boolean greaterThanOrEqual(String thisVersion, String targetVersion) {
		return compare(thisVersion, targetVersion) >= 0;
	}

	/**
	 * thisVersion 小于 targetVersion
	 */
	public static boolean lessThan(String thisVersion, String targetVersion) {
		return compare(thisVersion, targetVersion) < 0;
	}

	/**
	 * thisVersion 小于等于 targetVersion
	 */
	public static boolean lessThanOrEqual(String thisVersion, String targetVersion) {
		return compare(thisVersion, targetVersion) <= 0;
	}

	public static boolean equals(String thisVersion, String targetVersion) {
		return compare(thisVersion, targetVersion) == 0;
	}
}
