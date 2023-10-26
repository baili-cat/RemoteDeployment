package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.javaAgent.JavaAgentConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:06 下午
 */
@Data
public class DeployJavaAgent {
    private ServerConfig serverConfig;
    private WgetConfig wgetConfig;
    private JavaAgentConfig javaAgentConfig;
}
