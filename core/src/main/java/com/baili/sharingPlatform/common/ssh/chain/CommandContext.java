/*
 * Created by baili on 2022/04/18.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.ssh.JSchExecutor;

import java.util.HashMap;

/**
 * @author baili
 * @date 2022/04/18.
 */
public class CommandContext extends HashMap<String, Object> {

	private CommandLogger logger;
	private JSchExecutor executor;

	public CommandContext(CommandLogger logger, JSchExecutor executor) {
		this.logger = logger;
		this.executor = executor;
	}

	public CommandLogger getLogger() {
		return logger;
	}

	public JSchExecutor getExecutor() {
		return executor;
	}
}
