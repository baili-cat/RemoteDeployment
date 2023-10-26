package com.baili.sharingPlatform.web.requestModel.docker;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.model.dockerModel.DockerComposeConfig.MiddlewareDockerConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:06 下午
 */
@Data
public class DeployMiddlewareDocker {
    private ServerConfig serverConfig;
    private MiddlewareDockerConfig middlewareDockerConfig;
}
