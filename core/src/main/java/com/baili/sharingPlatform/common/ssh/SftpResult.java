package com.baili.sharingPlatform.common.ssh;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class SftpResult<T> extends ExecuteResult<T> {

	SftpResult(T result) {
		super(result);
	}

	SftpResult(SshException ex) {
		super(ex);
	}

	public static <T> SftpResult<T> ok() {
		return ok(null);
	}

	public static <T> SftpResult<T> ok(T result) {
		return new SftpResult<>(result);
	}

	public static <T> SftpResult<T> fail(SshException ex) {
		return new SftpResult<>(ex);
	}
}
