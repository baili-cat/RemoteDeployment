/*
 * Created by baili on 2020/11/11.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.common.ssh.ExecResult;
import com.baili.sharingPlatform.common.ssh.ExecuteResult;
import com.baili.sharingPlatform.common.ssh.JSchExecutor;
import com.baili.sharingPlatform.common.ssh.SshException;
import com.baili.sharingPlatform.common.utils.DateUtils;
import com.baili.sharingPlatform.common.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 命令执行链
 *
 * @author baili
 * @date 2020/11/11.
 */
@Slf4j
public class CommandChain {

	private CommandLogger       logger;
	private LinkedList<Command> commands = new LinkedList<>();
	private CommandContext      context;

	public CommandChain(JSchExecutor executor, CommandLogger logger) {
		this(executor, logger, null);
	}


	public CommandChain(JSchExecutor executor, CommandLogger logger, List<Command> commands) {
		this.logger = Optional.ofNullable(logger).orElse(new Slf4jCommandLogger(LoggerFactory.getLogger(CommandChain.class)));
		this.context = new CommandContext(logger, executor);
		if (commands != null) {
			this.commands = new LinkedList<>(commands);
		}
	}

	/**
	 * 添加命令
	 */
	public void add(Command command) {
		commands.add(command);
	}

	/**
	 * 添加命令
	 */
	public void add(List<Command> commands) {
		this.commands.addAll(commands);
	}

	/**
	 * 执行命令链
	 */
	public boolean invoke() {
		LinkedList<Command> rollbackCommands = new LinkedList<>();
		for (Command command : commands) {
			try {
				if (command.getCondition() != null && !command.getCondition().accept(context)) {
					logger.info(String.format("[%s] - skip execute", command.getName()));
					continue;
				}
			} catch (Throwable e) {
				Throwable cause = ExceptionUtils.getRealCause(e);
				if (command.getFailPolicy() == FailPolicy.Ignore) {
					logger.warn(
							String.format("[%s] - condition failed(ignore), cause: (%s)%s", command.getName(), cause.getClass(),
									cause.getMessage()));
				} else if (command.getFailPolicy() == FailPolicy.Break) {
					logger.warn(
							String.format("[%s] - condition failed(break), cause: (%s)%s", command.getName(), cause.getClass(), cause.getMessage()));
					return true;
				} else if (command.getFailPolicy() == FailPolicy.Interrupt) {
					logger.error(String.format("[%s] - condition failed(interrupt), cause: (%s)%s", command.getName(), cause.getClass(),
							cause.getMessage()));
					doRollbacks(rollbackCommands);
					return false;
				}
			}

			logger.info(String.format("[%s] - executing command: \"%s\"", command.getName(), command));

			long start = System.currentTimeMillis();
			ExecuteResult<?> result = command.invoke(context);
			long elapsedTime = System.currentTimeMillis() - start;

			// 添加到滚命令集合
			if (command.getRollback() != null) {
				rollbackCommands.addFirst(command);
			}
			// 打印命令输出
			if (result instanceof ExecResult) {
				logExecOut((ExecResult<?>)result);
			}

			if (result.isSuccess()) {
				logger.info(String.format("[%s] - finished(%s)", command.getName(), DateUtils.toDurationString(elapsedTime)));
			} else {
				SshException ex = result.getError();
				if (ex.getCause() != null && !(ex.getCause() instanceof TestCaseException)) {
					log.error("[{}] - command \"{}\" execution error", command.getName(), ex.getCommand(), ex);
				}

				Throwable cause = ExceptionUtils.getRealCause(ex);
				if (command.getFailPolicy() == FailPolicy.Ignore) {
					logger.warn(String.format("[%s] - failed(ignore), cause: (%s)%s", command.getName(), cause.getClass(), cause.getMessage()));
				} else if (command.getFailPolicy() == FailPolicy.Break) {
					logger.warn(String.format("[%s] - failed(break), cause: (%s)%s", command.getName(), cause.getClass(), cause.getMessage()));
					return true;
				} else if (command.getFailPolicy() == FailPolicy.Interrupt) {
					logger.error(String.format("[%s] - failed(interrupt), cause: (%s)%s", command.getName(), cause.getClass(), cause.getMessage()));
					doRollbacks(rollbackCommands);
					return false;
				}
			}
		}
		return true;
	}

	private void logExecOut(ExecResult<?> result) {
		// TODO 远程命令执行输出内容，特殊字符转换处理
		if (StringUtils.isNotBlank(result.getStdOut())) {
			logger.stdOut(result.getStdOut());
		}
		if (StringUtils.isNotBlank(result.getErrOut())) {
			logger.errOut(result.getErrOut());
		}
	}

	private void doRollbacks(List<Command> commands) {
		logger.info(String.format("Rollback commands: %s", commands.stream().map(Command::getName).collect(Collectors.toList())));
		for (Command command : commands) {
			logger.info(String.format("[Rollback:%s] - executing command: \"%s\"", command.getName(), command.toString()));

			long start = System.currentTimeMillis();
			ExecuteResult<?> result = command.getRollback().invoke(context);
			long elapsedTime = System.currentTimeMillis() - start;

			/*if (result instanceof ExecResult) {
				ExecResult<?> er = (ExecResult<?>)result;
				if (StringUtils.isNotBlank(er.getStdOut())) {
					logger.stdOut(er.getStdOut());
				}
				if (StringUtils.isNotBlank(er.getErrOut())) {
					logger.errOut(er.getErrOut());
				}
			}*/

			if (result.isSuccess()) {
				logger.info(String.format("[Rollback:%s] - finished(%s)", command.getName(), DateUtils.toDurationString(elapsedTime)));
			} else {
				SshException ex = result.getError();
				if (ex.getCause() != null && !(ex.getCause() instanceof TestCaseException)) {
					log.error("[{}] - Rollback command \"{}\" execution error", command.getName(), ex.getCommand(), ex);
				}

				String cause = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
				logger.warn(String.format("[Rollback:%s] - failed, cause: %s", command.getName(), cause));
			}
		}
	}
}
