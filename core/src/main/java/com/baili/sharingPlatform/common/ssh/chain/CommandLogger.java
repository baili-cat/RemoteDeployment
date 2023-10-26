/*
 * Created by baili on 2020/11/12.
 */
package com.baili.sharingPlatform.common.ssh.chain;

/**
 * @author baili
 * @date 2020/11/12.
 */
public interface CommandLogger {

	default void stdOut(String out) {}

	default void errOut(String out) {}

	default void debug(String msg) {}

	default void info(String msg) {}

	default void warn(String msg) {}

	default void error(String msg) {}

}
