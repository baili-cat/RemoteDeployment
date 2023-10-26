/*
 * Created by baili on 2020/10/29.
 */
package com.baili.sharingPlatform.common.ssh;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class ExecuteResult<T> {

	private T            result;
	private SshException error;

	ExecuteResult(T result) {
		this.result = result;
	}

	ExecuteResult(SshException ex) {
		this.error = ex;
	}

	public boolean isSuccess() {
		return error == null;
	}

	public T getResult() {
		return result;
	}

	public SshException getError() {
		return error;
	}
}
