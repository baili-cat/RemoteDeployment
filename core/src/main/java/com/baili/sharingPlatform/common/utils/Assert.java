/*
 * Created by baili on 2020/11/23.
 */
package com.baili.sharingPlatform.common.utils;

import com.baili.sharingPlatform.common.TestCaseException;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author baili
 * @date 2020/11/23.
 */
public final class Assert {

	private Assert() {
	}

	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new TestCaseException(message);
		}
	}

	public static void hasLength(@Nullable String text, String message) {
		if (!StringUtils.hasLength(text)) {
			throw new TestCaseException(message);
		}
	}

	public static void hasText(@Nullable String text, String message) {
		if (!StringUtils.hasText(text)) {
			throw new TestCaseException(message);
		}
	}

	public static void isNull(@Nullable Object object, String message) {
		if (object != null) {
			throw new TestCaseException(message);
		}
	}

	public static void notNull(@Nullable Object object, String message) {
		if (object == null) {
			throw new TestCaseException(message);
		}
	}

	public static void notEmpty(@Nullable Object[] array, String message) {
		if (ObjectUtils.isEmpty(array)) {
			throw new TestCaseException(message);
		}
	}

	public static void notEmpty(@Nullable Collection<?> collection, String message) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new TestCaseException(message);
		}
	}

	public static void notEmpty(@Nullable Map<?, ?> map, String message) {
		if (CollectionUtils.isEmpty(map)) {
			throw new TestCaseException(message);
		}
	}

	public static void noNullElements(@Nullable Object[] array, String message) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					throw new TestCaseException(message);
				}
			}
		}

	}

	public static void noNullElements(@Nullable Collection<?> collection, String message) {
		if (collection != null) {
			for (Object element : collection) {
				if (element == null) {
					throw new IllegalArgumentException(message);
				}
			}
		}
	}
}
