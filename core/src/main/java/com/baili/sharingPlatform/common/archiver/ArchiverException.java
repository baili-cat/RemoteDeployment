/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver;

/**
 * @author baili
 * @date 2020/12/01.
 */
public class ArchiverException extends RuntimeException {

	public ArchiverException() {
	}

	public ArchiverException(String message) {
		super(message);
	}

	public ArchiverException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiverException(Throwable cause) {
		super(cause);
	}

	public ArchiverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
