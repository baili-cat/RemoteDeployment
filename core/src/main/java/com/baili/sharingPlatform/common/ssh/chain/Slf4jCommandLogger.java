/*
 * Created by baili on 2020/12/04.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

/**
 * @author baili
 * @date 2020/12/04.
 */
@Slf4j
public class Slf4jCommandLogger implements CommandLogger {

	private Logger logger;

	public Slf4jCommandLogger(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void stdOut(String out) {
		logger.info("***********************[STD_OUT]***********************\n{}", out);
	}

	@Override
	public void errOut(String out) {
		logger.info("***********************[ERR_OUT]***********************\n{}", out);
	}

	@Override
	public void debug(String msg) {
		logger.debug(msg);
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	@Override
	public void warn(String msg) {
		logger.warn(msg);
	}

	@Override
	public void error(String msg) {
		logger.error(msg);
	}

}
