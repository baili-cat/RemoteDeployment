package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.javaAgent.JavaAgentConfig;
import com.baili.sharingPlatform.model.mockServer.MockApplicationConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:06 下午
 */
@Data
public class DeployMockApplicationMountJavaAgent {
    private ServerConfig serverConfig;
    private WgetConfig wgetConfig;
    private JavaAgentConfig javaAgentConfig;
    private MockApplicationConfig mockApplicationConfig;
    private String actionType;
}
