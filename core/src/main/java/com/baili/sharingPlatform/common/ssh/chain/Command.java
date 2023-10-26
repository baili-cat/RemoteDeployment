/*
 * Created by baili on 2020/11/11.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.ssh.ExecuteResult;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baili
 * @date 2020/11/11.
 */
@SuperBuilder
public abstract class Command {

	// 命令名称
	private String        name;
	// 命令执行失败处理策略
	private FailPolicy    failPolicy;
	// 命令执行失败回滚方法
	private Rollback      rollback;
	// 命令执行条件
	private Condition     condition;
	// 子命令
	private List<Command> childs;

	public Command(String name, FailPolicy failPolicy) {
		this.name = name;
		this.failPolicy = failPolicy;
	}

	public abstract ExecuteResult<?> invoke(CommandContext ctx);

	public String getName() {
		return name;
	}

	public FailPolicy getFailPolicy() {
		return failPolicy;
	}

	public Rollback getRollback() {
		return rollback;
	}

	public void setRollback(Rollback rollback) {
		this.rollback = rollback;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public List<Command> getChilds() {
		return childs;
	}

	public void setChilds(List<Command> childs) {
		this.childs = childs;
	}

	public Command addCmd(Command command) {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		childs.add(command);
		return this;
	}
}
