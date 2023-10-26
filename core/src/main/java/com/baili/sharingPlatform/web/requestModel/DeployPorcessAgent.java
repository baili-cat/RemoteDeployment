package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.processAgent.ProcessAgentConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日6:56 下午
 */
@Data
public class DeployPorcessAgent {
    private ServerConfig serverConfig;
    private WgetConfig wgetConfig;
    private ProcessAgentConfig processAgentConfig;
}
