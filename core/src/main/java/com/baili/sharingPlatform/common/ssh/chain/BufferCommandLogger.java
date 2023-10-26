/*
 * Created by baili on 2020/12/04.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import lombok.extern.slf4j.Slf4j;

/**
 * @author baili
 * @date 2020/12/04.
 */
@Slf4j
public class BufferCommandLogger implements CommandLogger {

	private StringBuilder builder = new StringBuilder();

	@Override
	public void stdOut(String out) {
		builder.append("[STD_OUT]: ").append(out).append("\n");
	}

	@Override
	public void errOut(String out) {
		builder.append("[ERR_OUT]: ").append(out).append("\n");
	}

	@Override
	public void debug(String msg) {
		builder.append("[DEBUG]: ").append(msg).append("\n");
	}

	@Override
	public void info(String msg) {
		builder.append("[INFO]: ").append(msg).append("\n");
	}

	@Override
	public void warn(String msg) {
		builder.append("[WARN]: ").append(msg).append("\n");
	}

	@Override
	public void error(String msg) {
		builder.append("[ERROR]: ").append(msg).append("\n");
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
