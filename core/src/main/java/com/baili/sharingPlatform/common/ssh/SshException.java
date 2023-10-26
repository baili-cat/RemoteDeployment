/*
 * Created by baili on 2020/11/11.
 */
package com.baili.sharingPlatform.common.ssh;

/**
 * @author baili
 * @date 2020/11/11.
 */
public class SshException extends RuntimeException {

	private boolean runtime;
	private String  command;

	public SshException(String command, String message) {
		super(message);
		this.command = command;
		this.runtime = false;
	}

	public SshException(String command, Throwable cause) {
		super(cause);
		this.command = command;
		this.runtime = true;
	}

	public String getCommand() {
		return command;
	}

	public boolean isRuntime() {
		return runtime;
	}
}
