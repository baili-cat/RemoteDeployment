/*
 * Created by baili on 2020/11/11.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.ssh.*;

/**
 * @author baili
 * @date 2020/11/11.
 */
public class SftpCmd extends Command {

	private Invoker invoker;
	private String  descr;

	public SftpCmd(String name, FailPolicy failPolicy, String descr, Invoker invoker) {
		super(name, failPolicy);
		this.invoker = invoker;
		this.descr = descr;
	}

	public SftpCmd(String name, FailPolicy failPolicy, String descr, Invoker invoker, Rollback rollback) {
		super(name, failPolicy);
		this.setRollback(rollback);
		this.invoker = invoker;
		this.descr = descr;
	}

	@Override
	public ExecuteResult<?> invoke(CommandContext context) {
		try {
			return invoker.invoke(context, context.getExecutor().sftp());
		} catch (Throwable e) {
			return SftpResult.fail(new SshException(descr, e));
		}
	}

	@Override
	public String toString() {
		if (descr != null) {
			return descr;
		}
		return super.toString();
	}

	public interface Invoker {

		SftpResult<?> invoke(CommandContext context, SftpExecutor sftp) throws Throwable;
	}
}
