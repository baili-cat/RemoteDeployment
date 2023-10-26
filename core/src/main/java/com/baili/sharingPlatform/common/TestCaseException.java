/*
 * Created by baili on 2020/10/29.
 */
package com.baili.sharingPlatform.common;

/**
 * @author baili
 * @date 2022年04月02日1:56 下午
 */
public class TestCaseException extends RuntimeException {

	public TestCaseException(String message) {
		super(message);
	}

	public TestCaseException(String message, Object... args) {
		super(String.format(message, args));
	}

	public TestCaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
