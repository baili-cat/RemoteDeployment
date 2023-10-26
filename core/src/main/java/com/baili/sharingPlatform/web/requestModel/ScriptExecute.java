package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:13 下午
 */
@Data
public class ScriptExecute {
    private ServerConfig serverConfig;
    private String script;
}
