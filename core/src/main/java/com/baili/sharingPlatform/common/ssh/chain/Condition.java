/*
 * Created by baili on 2021/02/23.
 */
package com.baili.sharingPlatform.common.ssh.chain;

/**
 * @author baili
 * @date 2021/02/23.
 */
public interface Condition {

	boolean accept(CommandContext context);
}
