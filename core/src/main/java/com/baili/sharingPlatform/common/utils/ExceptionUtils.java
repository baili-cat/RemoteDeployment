/*
 * Created by baili on 2021/02/23.
 */
package com.baili.sharingPlatform.common.utils;

/**
 * @author baili
 * @date 2021/02/23.
 */
public class ExceptionUtils {

	public static Throwable getRealCause(Throwable e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}
}
