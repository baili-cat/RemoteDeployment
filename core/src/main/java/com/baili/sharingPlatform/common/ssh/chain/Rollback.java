/*
 * Created by baili on 2020/11/26.
 */
package com.baili.sharingPlatform.common.ssh.chain;

import com.baili.sharingPlatform.common.ssh.ExecuteResult;

/**
 * @author baili
 * @date 2020/11/26.
 */
public interface Rollback {

	ExecuteResult<?> invoke(CommandContext context);
}

