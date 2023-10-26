/*
 * Created by baili on 2020/11/11.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.ssh.ExecConfig;
import com.baili.sharingPlatform.common.ssh.ExecuteResult;
import com.baili.sharingPlatform.common.ssh.ResultConverter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

/**
 * @author baili
 * @date 2020/11/11.
 */
@SuperBuilder
public class ExecCmd extends Command {

	// 执行命令
	private String                command;
	// 命令执行超时时间
	private Duration              timeout;
	// 命令执行结果转换器
	private ResultConverter<Void> converter;
	// Exec配置
	private ExecConfig config;

	public ExecCmd(String name, FailPolicy failPolicy, String command, Duration timeout) {
		this(name, failPolicy, command, timeout, null, null);
	}

	public ExecCmd(String name, FailPolicy failPolicy, String command, Duration timeout, Rollback rollback) {
		this(name, failPolicy, command, timeout, rollback, null);
	}

	public ExecCmd(String name, FailPolicy failPolicy, String command, Duration timeout, Rollback rollback, ExecConfig config) {
		super(name, failPolicy);
		this.setRollback(rollback);
		this.command = command;
		this.timeout = timeout;
		this.config = config;
	}

	public ExecCmd setConverter(ResultConverter<Void> converter) {
		this.converter = converter;
		return this;
	}

	public ExecCmd setConfig(ExecConfig config) {
		this.config = config;
		return this;
	}

	@Override
	public ExecuteResult<?> invoke(CommandContext context) {
		return context.getExecutor().exec().execute(command, timeout, converter, config);
	}

	@Override
	public String toString() {
		return command;
	}
}
