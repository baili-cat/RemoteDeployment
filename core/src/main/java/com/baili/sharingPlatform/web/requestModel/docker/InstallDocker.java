package com.baili.sharingPlatform.web.requestModel.docker;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.model.dockerModel.DockerInitConfig.DockerInstallConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年11月14日14:55
 */
@Data
public class InstallDocker {
    private ServerConfig serverConfig;
    private DockerInstallConfig dockerInstallConfig;
}
