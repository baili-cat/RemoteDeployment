/*
 * Created by baili on 2020/10/29.
 */
package com.baili.sharingPlatform.common.ssh;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class ExecResult<T> extends ExecuteResult<T> {

	private Integer extCode;
	private String  stdOut;
	private String  errOut;

	ExecResult(T result, int extCode, String stdOut, String errOut) {
		super(result);
		this.extCode = extCode;
		this.stdOut = stdOut;
		this.errOut = errOut;
	}

	ExecResult(SshException ex, int extCode, String stdOut, String errOut) {
		super(ex);
		this.extCode = extCode;
		this.stdOut = stdOut;
		this.errOut = errOut;
	}

	public static <T> ExecResult<T> ok(T result, int extCode, String stdOut, String errOut) {
		return new ExecResult<>(result, extCode, stdOut, errOut);
	}

	public static <T> ExecResult<T> fail(SshException ex, int extCode, String stdOut, String errOut) {
		return new ExecResult<>(ex, extCode, stdOut, errOut);
	}

	public Integer getExtCode() {
		return extCode;
	}

	public String getStdOut() {
		return stdOut;
	}

	public String getErrOut() {
		return errOut;
	}
}
